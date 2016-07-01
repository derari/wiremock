package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.lang.reflect.Constructor;

@JsonDeserialize(using = StringValuePatternJsonDeserializer.class)
public abstract class StringValuePattern implements ValueMatcher<String> {

    public static final StringValuePattern ABSENT = new StringValuePattern(null) {
        @Override
        public MatchResult match(String value) {
            return MatchResult.noMatch();
        }
    };

    protected final String expectedValue;

    public StringValuePattern(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    @JsonIgnore
    public boolean isPresent() {
        return this != ABSENT;
    }

    public Boolean isAbsent() {
        return this != ABSENT ? null : true;
    }

    @JsonIgnore
    public Boolean nullSafeIsAbsent() {
        return this == ABSENT;
    }

    @JsonIgnore
    public String getValue() {
        return expectedValue;
    }

    @Override
    public String toString() {
        return getName() + " " + getValue();
    }

    public final String getName() {
        Constructor<?> constructor =
            FluentIterable.of(this.getClass().getDeclaredConstructors()).firstMatch(new Predicate<Constructor<?>>() {
            @Override
            public boolean apply(Constructor<?> input) {
                return (input.getParameterAnnotations().length > 0 &&
                        input.getParameterAnnotations()[0].length > 0 &&
                        input.getParameterAnnotations()[0][0] instanceof JsonProperty);
            }
        }).orNull();

        if (constructor == null) {
            throw new IllegalStateException("Constructor must have a first parameter annotatated with JsonProperty(\"<operator name>\")");
        }
        JsonProperty jsonPropertyAnnotation = (JsonProperty) constructor.getParameterAnnotations()[0][0];
        return jsonPropertyAnnotation.value();
    }

    @Override
    public String getExpected() {
        return getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringValuePattern that = (StringValuePattern) o;
        return Objects.equal(expectedValue, that.expectedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(expectedValue);
    }
}
