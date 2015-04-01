/*
 * Copyright (c) 2015. David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.core.user.opt;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.q3c.krail.core.user.opt.cache.OptionCacheKey;
import uk.q3c.krail.core.user.opt.cache.OptionKeyException;
import uk.q3c.krail.core.user.profile.RankOption;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data Access Object for {@link InMemoryOptionStore}
 * <p>
 * Created by David Sowerby on 20/02/15.
 */
public class InMemoryOptionDao implements OptionDao {

    private static Logger log = LoggerFactory.getLogger(InMemoryOptionDao.class);
    private InMemoryOptionStore optionStore;

    @Inject
    public InMemoryOptionDao(InMemoryOptionStore optionStore) {
        this.optionStore = optionStore;
    }

    @Override
    public void write(@Nonnull OptionCacheKey cacheKey, @Nonnull Object value) {
        checkRankOption(cacheKey, RankOption.SPECIFIC_RANK);
        checkNotNull(value);
        String hierarchyName = cacheKey.getHierarchy()
                                       .persistenceName();

        String rankName = cacheKey.getRequestedRankName();
        OptionKey optionKey = cacheKey.getOptionKey();
        optionStore.setValue(hierarchyName, rankName, optionKey, value);
    }

    @Override
    public Object deleteValue(@Nonnull OptionCacheKey cacheKey) {
        checkRankOption(cacheKey, RankOption.SPECIFIC_RANK);
        String hierarchyName = cacheKey.getHierarchy()
                                       .persistenceName();

        String rankName = cacheKey.getRequestedRankName();
        OptionKey optionKey = cacheKey.getOptionKey();
        return optionStore.deleteValue(hierarchyName, rankName, optionKey);
    }

    @Override
    public Optional<Object> getValue(@Nonnull OptionCacheKey cacheKey) {
        checkRankOption(cacheKey, RankOption.SPECIFIC_RANK);
        String hierarchyName = cacheKey.getHierarchy()
                                       .persistenceName();

        String rankName = cacheKey.getRequestedRankName();
        OptionKey optionKey = cacheKey.getOptionKey();
        Object result = optionStore.getValue(hierarchyName, rankName, optionKey);
        if (result == null) {
            return Optional.empty();
        } else {
            return Optional.of(result);
        }

    }

    /**
     * Returns the highest ranked value available for the {@code cacheKey}
     *
     * @param cacheKey
     *         they key to look for
     *
     * @return the highest ranked value available for the {@code cacheKey}
     *
     * @throws OptionKeyException
     *         if cacheKey {@link RankOption} is not equal to {@link RankOption#HIGHEST_RANK}
     */
    @Nonnull
    @Override
    public Optional<Object> getHighestRankedValue(@Nonnull OptionCacheKey cacheKey) {
        checkRankOption(cacheKey, RankOption.HIGHEST_RANK);
        ImmutableList<String> ranks = cacheKey.getHierarchy()
                                              .ranksForCurrentUser();
        LinkedHashMap<String, Object> values = getValuesForRanks(cacheKey, ranks);
        for (String rank : ranks) {
            if (values.containsKey(rank)) {
                return Optional.of(values.get(rank));
            }
        }
        return Optional.empty();
    }

    @Nonnull
    protected LinkedHashMap<String, Object> getValuesForRanks(@Nonnull OptionCacheKey cacheKey, List<String> rankNames) {


        Map<String, Object> valueMapForOptionKey = optionStore.valueMapForOptionKey(cacheKey.getHierarchy()
                                                                                            .persistenceName(), rankNames, cacheKey.getOptionKey());


        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();

        //shortcut to doing nothing
        if (valueMapForOptionKey.isEmpty()) {
            return resultMap;
        }

        //select the rank names-value if it is in the user's rank names

        //not so easy to use Stream API, despite the IDE prompt, and not worth the effort
        // main challenge is that order here is important, and Stream forEach does not guarantee processing order
        for (String rankName : rankNames) {
            if (valueMapForOptionKey.containsKey(rankName)) {
                resultMap.put(rankName, valueMapForOptionKey.get(rankName));
            }
        }
        return resultMap;
    }

    /**
     * Returns the lowest ranked value available for the {@code cacheKey}
     *
     * @param cacheKey
     *         they key to look for
     *
     * @return the lowest ranked value available for the {@code cacheKey}
     *
     * @throws OptionKeyException
     *         if cacheKey {@link RankOption} is not equal to {@link RankOption#LOWEST_RANK}
     */
    @Nonnull
    @Override
    public Optional<Object> getLowestRankedValue(@Nonnull OptionCacheKey cacheKey) {
        checkRankOption(cacheKey, RankOption.LOWEST_RANK);
        ImmutableList<String> ranks = cacheKey.getHierarchy()
                                              .ranksForCurrentUser();
        LinkedHashMap<String, Object> values = getValuesForRanks(cacheKey, ranks);
        ImmutableList<String> reversedRanks = ranks.reverse();
        for (String rank : reversedRanks) {
            if (values.containsKey(rank)) {
                return Optional.of(values.get(rank));
            }
        }
        return Optional.empty();
    }
}
