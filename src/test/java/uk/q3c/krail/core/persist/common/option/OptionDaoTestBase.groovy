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

package uk.q3c.krail.core.persist.common.option

import com.google.common.collect.ImmutableList
import spock.lang.Specification
import uk.q3c.krail.core.data.DefaultOptionStringConverter
import uk.q3c.krail.core.data.OptionStringConverter
import uk.q3c.krail.core.i18n.LabelKey
import uk.q3c.krail.core.option.OptionKey
import uk.q3c.krail.core.option.OptionKeyException
import uk.q3c.krail.core.persist.cache.option.OptionCacheKey
import uk.q3c.krail.core.user.profile.UserHierarchy
import uk.q3c.krail.core.view.component.LocaleContainer

import static uk.q3c.krail.core.user.profile.RankOption.*

/**
 * Created by David Sowerby on 27 Jan 2016
 */
class OptionDaoTestBase extends Specification {

    OptionStringConverter optionStringConverter
    OptionDao dao
    String expectedConnectionUrl
    OptionKey<Integer> optionKey1 = Mock();
    OptionKey<Integer> optionKey2 = Mock();
    OptionCacheKey cacheKeySpecific11 = Mock()
    OptionCacheKey cacheKeySpecific10 = Mock()
    OptionCacheKey cacheKeySpecific12 = Mock()
    OptionCacheKey cacheKeySpecific2 = Mock()
    OptionCacheKey cacheKeyHigh1 = Mock()
    OptionCacheKey cacheKeyLow1 = Mock()
    UserHierarchy hierarchy1 = Mock()

    def setup() {
        optionStringConverter = new DefaultOptionStringConverter()
        optionKey1.getContext() >> LocaleContainer.class
        optionKey1.getDefaultValue() >> 99
        optionKey1.getKey() >> LabelKey.Yes
        optionKey1.compositeKey() >> 'composite key 1'
        optionKey2.getContext() >> LocaleContainer.class
        optionKey2.getDefaultValue() >> 109
        optionKey2.getKey() >> LabelKey.No
        optionKey2.compositeKey() >> 'composite key 2'


        cacheKeySpecific10.getRankOption() >> SPECIFIC_RANK
        cacheKeySpecific10.getOptionKey() >> optionKey1
        cacheKeySpecific10.getHierarchy() >> hierarchy1
        cacheKeySpecific10.getRequestedRankName() >> 'level 0'

        cacheKeySpecific11.getRankOption() >> SPECIFIC_RANK
        cacheKeySpecific11.getOptionKey() >> optionKey1
        cacheKeySpecific11.getHierarchy() >> hierarchy1
        cacheKeySpecific11.getRequestedRankName() >> 'level 1'

        cacheKeySpecific12.getRankOption() >> SPECIFIC_RANK
        cacheKeySpecific12.getOptionKey() >> optionKey1
        cacheKeySpecific12.getHierarchy() >> hierarchy1
        cacheKeySpecific12.getRequestedRankName() >> 'level 2'


        cacheKeySpecific2.getRankOption() >> SPECIFIC_RANK
        cacheKeySpecific2.getOptionKey() >> optionKey2
        cacheKeySpecific2.getHierarchy() >> hierarchy1
        cacheKeySpecific2.getRequestedRankName() >> 'level 0'

        cacheKeyHigh1.getRankOption() >> HIGHEST_RANK
        cacheKeyHigh1.getOptionKey() >> optionKey1
        cacheKeyHigh1.getHierarchy() >> hierarchy1
        cacheKeyHigh1.getRequestedRankName() >> 'level 1'

        cacheKeyLow1.getRankOption() >> LOWEST_RANK
        cacheKeyLow1.getOptionKey() >> optionKey1
        cacheKeyLow1.getHierarchy() >> hierarchy1
        cacheKeyLow1.getRequestedRankName() >> 'level 1'

        hierarchy1.persistenceName() >> 'hierarchy1'
        hierarchy1.rankName(0) >> 'level 0'
        hierarchy1.rankName(1) >> 'level 1'
        hierarchy1.rankName(2) >> 'level 2'
        hierarchy1.lowestRank() >> 2
        hierarchy1.lowestRankName() >> 'level 2'
        hierarchy1.highestRankName() >> 'level 0'
        hierarchy1.ranksForCurrentUser() >> ImmutableList.of('level 0', 'level 1', 'level 2')

    }

    def "connectionUrl"() {
        expect:
        dao.connectionUrl().equals(expectedConnectionUrl)
    }

    def "write with empty optional throws exception"() {
        when:
        dao.write(cacheKeySpecific11, Optional.empty())

        then:
        thrown(IllegalArgumentException)
    }

    def "write with rankOption HIGHEST throws exception"() {
        when:
        dao.write(cacheKeyHigh1, Optional.of(33))

        then:
        thrown(OptionKeyException)
    }


    def "write with rankOption LOWEST throws exception"() {
        when:
        dao.write(cacheKeyLow1, Optional.of(33))

        then:
        thrown(OptionKeyException)
    }

    def "write and get specific"() {
        when:
        dao.write(cacheKeySpecific11, Optional.of(23))

        then:
        dao.getValue(cacheKeySpecific11).equals(Optional.of(23))
    }

    def "write, 2 different values for same key, latest is returned"() {

        when:
        dao.write(cacheKeySpecific11, Optional.of(33))
        dao.write(cacheKeySpecific11, Optional.of(232))

        then:
        dao.getValue(cacheKeySpecific11).equals(Optional.of(232))
    }

    def "count"() {

        when:
        dao.write(cacheKeySpecific11, Optional.of(33))
        dao.write(cacheKeySpecific11, Optional.of(232))
        dao.write(cacheKeySpecific2, Optional.of(232))

        then:
        dao.count() == 2 // 1 duplicated
    }

    def "clear"() {
        given:
        dao.write(cacheKeySpecific11, Optional.of(33))
        dao.write(cacheKeySpecific11, Optional.of(232))
        dao.write(cacheKeySpecific2, Optional.of(232))

        when:
        dao.clear()

        then:
        dao.count() == 0
    }

    def "delete value, key not specific, throws exception"() {
        when:
        dao.deleteValue(cacheKeyHigh1)

        then:
        thrown(OptionKeyException)
    }

    def "delete value, did not exist, returns Optional.empty()"() {
        expect:
        dao.deleteValue(cacheKeySpecific11).equals(Optional.empty())
    }

    def "delete value, did exist, returns Optional.of(value)"() {
        given:
        dao.write(cacheKeySpecific11, Optional.of(33))

        when:
        Optional<String> result = dao.deleteValue(cacheKeySpecific11)

        then:
        result.equals(Optional.of('33'))
    }


    def "getHighest with 0 entries returns Optional.empty"() {
        expect:
        dao.getValue(cacheKeyHigh1).equals(Optional.empty())
    }

    def "getLowest with 0 entries returns Optional.empty"() {
        expect:
        dao.getValue(cacheKeyLow1).equals(Optional.empty())
    }

    def "getSpecific with 0 entries returns Optional.empty"() {
        expect:
        dao.getValue(cacheKeySpecific11).equals(Optional.empty())
    }

    def "getHighest with 1 entry returns correct value"() {
        when:
        dao.write(cacheKeySpecific11, Optional.of(33))

        then:
        dao.getValue(cacheKeyHigh1).equals(Optional.of(33))
    }

    def "getLowest with 1 entry returns correct value"() {
        when:
        dao.write(cacheKeySpecific11, Optional.of(33))

        then:
        dao.getValue(cacheKeyLow1).equals(Optional.of(33))
    }

    def "getSpecific with 1 entry returns correct value"() {
        when:
        dao.write(cacheKeySpecific11, Optional.of(33))

        then:
        dao.getValue(cacheKeySpecific11).equals(Optional.of(33))
    }

    def "getHighest with 3 entries returns correct value"() {
        when:
        dao.write(cacheKeySpecific10, Optional.of(4893))
        dao.write(cacheKeySpecific12, Optional.of(353))
        dao.write(cacheKeySpecific11, Optional.of(33))

        then:
        dao.getValue(cacheKeyHigh1).equals(Optional.of(4893))
    }

    def "getLowest with 3 entries returns correct value"() {
        when:
        dao.write(cacheKeySpecific10, Optional.of(4893))
        dao.write(cacheKeySpecific12, Optional.of(353))
        dao.write(cacheKeySpecific11, Optional.of(33))

        then:
        dao.getValue(cacheKeyLow1).equals(Optional.of(353))
    }

    def "getSpecific with 3 entries returns correct value"() {
        when:
        dao.write(cacheKeySpecific10, Optional.of(4893))
        dao.write(cacheKeySpecific12, Optional.of(353))
        dao.write(cacheKeySpecific11, Optional.of(33))

        then:
        dao.getValue(cacheKeySpecific12).equals(Optional.of(353))
    }
}
