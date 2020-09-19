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
package griffon.plugins.theme;

import griffon.annotations.core.Nonnull;
import griffon.core.properties.VetoablePropertySource;
import griffon.core.resources.ResourceResolver;
import griffon.exceptions.PropertyVetoException;

import java.util.Set;

/**
 * @author Andres Almiray
 */
public interface ThemeManager extends VetoablePropertySource {
    String PROPERTY_CURRENT_THEME = "currentTheme";

    @Nonnull
    Set<String> getThemes();

    @Nonnull
    String getCurrentTheme();

    void setCurrentTheme(@Nonnull String currentTheme) throws PropertyVetoException;

    @Nonnull
    ResourceResolver getResourceResolver();
}
