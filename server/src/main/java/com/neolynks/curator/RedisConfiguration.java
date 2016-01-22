package com.neolynks.curator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Created by nishantgupta on 22/1/16.
 */
@Data
public class RedisConfiguration {
    @NotEmpty
    @JsonProperty
    private String hostname;

    @Min(1)
    @Max(65535)
    @JsonProperty
    private Integer port;
}
