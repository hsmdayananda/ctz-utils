package ru.concerteza.util.db.springjdbc;

import com.google.common.base.Function;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: alexey
 * Date: 7/29/12
 */
public class MapParamsFunction implements Function<Map<String, ?>, SqlParameterSource> {
    public static Function<Map<String, ?>, SqlParameterSource> INSTANCE = new MapParamsFunction();

    @Override
    public MapSqlParameterSource apply(@Nullable Map<String, ?> input) {
        checkNotNull(input, "Provided bean is null");
        return new MapSqlParameterSource(input);
    }
}
