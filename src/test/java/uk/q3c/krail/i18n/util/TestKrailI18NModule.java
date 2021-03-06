/*
 *
 *  * Copyright (c) 2016. David Sowerby
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations under the License.
 *
 */

package uk.q3c.krail.i18n.util;


import uk.q3c.krail.core.i18n.KrailI18NModule;
import uk.q3c.krail.i18n.CurrentLocale;
import uk.q3c.krail.i18n.test.MockCurrentLocale;

import java.util.Locale;

/**
 * Different from
 */

public class TestKrailI18NModule extends KrailI18NModule {

    MockCurrentLocale currentLocale = new MockCurrentLocale();

    @Override
    protected void bindCurrentLocale() {
        bind(CurrentLocale.class).toInstance(currentLocale);
    }


    @Override
    protected void define() {
        super.define();
        supportedLocales(Locale.ITALY, Locale.UK, Locale.GERMANY);
        supportedLocales(new Locale("de", "CH"));
    }


}
