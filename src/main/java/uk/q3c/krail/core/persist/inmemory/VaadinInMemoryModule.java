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

package uk.q3c.krail.core.persist.inmemory;

import com.google.inject.TypeLiteral;
import com.vaadin.data.util.BeanItemContainer;
import uk.q3c.krail.core.option.inmemory.InMemoryOptionContainerProvider;
import uk.q3c.krail.core.option.inmemory.container.DefaultInMemoryOptionContainerProvider;
import uk.q3c.krail.option.persist.OptionContainerProvider;
import uk.q3c.krail.persist.InMemory;
import uk.q3c.krail.persist.VaadinContainerProvider;
import uk.q3c.krail.persist.inmemory.InMemoryModule;

/**
 * A pseudo persistence module which actually just stores things in memory maps - useful for testing
 * <p>
 * Created by David Sowerby on 25/06/15.
 */
public class VaadinInMemoryModule extends InMemoryModule {


    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        super.configure();


        bindOptionContainerProvider();

    }

    protected void bindOptionContainerProvider() {
        if (isProvideOptionDao() || isProvidePatternDao()) {
            bind(InMemoryOptionContainerProvider.class).to(DefaultInMemoryOptionContainerProvider.class);
            bind(OptionContainerProvider.class).annotatedWith(InMemory.class)
                    .to(InMemoryOptionContainerProvider.class);
            bindContainerProvider();
        }
    }


    protected void bindContainerProvider() {
        TypeLiteral<VaadinContainerProvider<BeanItemContainer>> containerProviderLiteral = new TypeLiteral<VaadinContainerProvider<BeanItemContainer>>() {
        };
        bind(containerProviderLiteral).annotatedWith(InMemory.class)
                .to(InMemoryContainerProvider.class);
    }


}
