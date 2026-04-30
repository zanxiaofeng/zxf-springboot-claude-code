package com.example.demo.apitest.support.json;

import lombok.experimental.UtilityClass;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

/**
 * Factory for creating JSON comparators with custom matching rules.
 * Ignores dynamic fields during assertion.
 */
@UtilityClass
public class JsonComparatorFactory {

    /**
     * Creates a JSON comparator for API response validation.
     * Ignores dynamic fields: timestamp, traceId, id, createdAt, updatedAt.
     */
    public JSONComparator buildApiResponseComparator() {
        return new CustomComparator(JSONCompareMode.LENIENT,
                customization("timestamp"),
                customization("traceId"),
                customization("id"),
                customization("createdAt"),
                customization("updatedAt"),
                customization("created_at"),
                customization("updated_at")
        );
    }

    private static Customization customization(String fieldName) {
        return Customization.customization(fieldName, new RegularExpressionValueMatcher<>(".*"));
    }
}
