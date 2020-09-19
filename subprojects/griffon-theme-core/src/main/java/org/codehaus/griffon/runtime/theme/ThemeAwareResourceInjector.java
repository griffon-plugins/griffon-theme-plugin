/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.theme;

import griffon.annotations.core.Nonnull;
import griffon.annotations.core.Nullable;
import griffon.core.GriffonApplication;
import griffon.core.events.DestroyInstanceEvent;
import griffon.core.resources.NoSuchResourceException;
import griffon.plugins.theme.ThemeAware;
import griffon.plugins.theme.ThemeManager;
import org.codehaus.griffon.runtime.core.resources.AbstractResourceInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.application.converter.ConverterRegistry;
import javax.application.event.EventHandler;
import javax.inject.Inject;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static griffon.core.GriffonApplication.PROPERTY_LOCALE;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class ThemeAwareResourceInjector extends AbstractResourceInjector {
    private static final Logger LOG = LoggerFactory.getLogger(ThemeAwareResourceInjector.class);
    private final InstanceStore instanceStore = new InstanceStore();
    private final GriffonApplication application;
    private final ThemeManager themeManager;

    @Inject
    public ThemeAwareResourceInjector(@Nonnull ConverterRegistry converterRegistry, @Nonnull GriffonApplication application, @Nonnull ThemeManager themeManager) {
        super(converterRegistry);
        this.application = requireNonNull(application, "Argument 'application' must not be null");
        this.themeManager = themeManager;

        application.getEventRouter().subscribe(this);

        themeManager.addPropertyChangeListener(ThemeManager.PROPERTY_CURRENT_THEME, event -> {
            LOG.info("Theme changed to {}", event.getNewValue());
            for (Object instance : instanceStore) {
                injectResources(instance);
            }
        });

        application.addPropertyChangeListener(PROPERTY_LOCALE, event -> {
            for (Object instance : instanceStore) {
                injectResources(instance);
            }
        });
    }

    @EventHandler
    public void handleDestroyInstanceEvent(@Nonnull DestroyInstanceEvent<?> event) {
        if (instanceStore.contains(event.getInstance())) {
            instanceStore.remove(event.getInstance());
        }
    }

    @Override
    public void injectResources(@Nonnull Object instance) {
        requireNonNull(instance, "Argument 'instance' must not be null");
        if (instance.getClass().getAnnotation(ThemeAware.class) == null) {
            super.injectResources(instance);
        } else {
            super.injectResources(instance);
            if (!instanceStore.contains(instance)) {
                instanceStore.add(instance);
            }
        }
    }

    @Override
    protected Object resolveResource(@Nonnull String key, @Nonnull String[] args) {
        try {
            return themeManager.getResourceResolver().resolveResource(key, args, application.getLocale());
        } catch (NoSuchResourceException nsre) {
            return application.getResourceResolver().resolveResource(key, args, application.getLocale());
        }
    }

    @Override
    protected Object resolveResource(@Nonnull String key, @Nonnull String[] args, @Nullable String defaultValue) {
        return themeManager.getResourceResolver().resolveResource(key, args, application.getLocale(), defaultValue);
    }

    private static class InstanceStore implements Iterable {
        private final List<WeakReference<Object>> instances = new LinkedList<>();

        private void add(Object instance) {
            if (null == instance) return;
            instances.add(new WeakReference<>(instance));
        }

        private void remove(Object instance) {
            if (null == instance) return;
            WeakReference<Object> subject = null;
            for (WeakReference<Object> instance1 : instances) {
                subject = instance1;
                Object candidate = subject.get();
                if (instance.equals(candidate)) {
                    break;
                }
            }
            if (subject != null) instances.remove(subject);
        }

        private boolean contains(Object instance) {
            if (null == instance) return false;
            for (WeakReference<Object> instance1 : instances) {
                Object candidate = instance1.get();
                if (instance.equals(candidate)) {
                    return true;
                }
            }
            return false;
        }

        public Iterator<Object> iterator() {
            final Iterator<WeakReference<Object>> it = instances.iterator();
            return new Iterator<Object>() {
                public boolean hasNext() {
                    return it.hasNext();
                }

                public Object next() {
                    return it.next().get();
                }

                public void remove() {
                    it.remove();
                }
            };
        }
    }
}
