package com.neolynks.curator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.neolynks.curator.core.Template;
import com.neolynks.vendor.model.CurationConfig;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.Collections;
import java.util.Map;

public class FastlaneConfiguration extends Configuration {
    @NotEmpty
    private String template;
    
    @NotNull
    private Long vendorId;
    
    @NotNull
    private Boolean clientConfig;

    @NotNull
    private Boolean serverConfig;

    @NotEmpty
    private String defaultName = "Stranger";

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();
    
    @NotNull
    private CurationConfig curationConfig = new CurationConfig();

    @JsonProperty("CurationConfig")
    public CurationConfig getCurationConfig() {
		return curationConfig;
	}

    @JsonProperty("CurationConfig")
	public void setCurationConfig(CurationConfig curationConfig) {
		this.curationConfig = curationConfig;
	}

	@NotNull
    private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();
	
	@JsonProperty
    public Boolean getClientConfig() {
		return clientConfig;
	}

    @JsonProperty
	public void setClientConfig(Boolean clientConfig) {
		this.clientConfig = clientConfig;
	}

	@JsonProperty
	public Boolean getServerConfig() {
		return serverConfig;
	}

	@JsonProperty
	public void setServerConfig(Boolean serverConfig) {
		this.serverConfig = serverConfig;
	}

	@JsonProperty
    public Long getVendorId() {
        return vendorId;
    }

    @JsonProperty
    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
    
    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public Template buildTemplate() {
        return new Template(template, defaultName);
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    @JsonProperty("viewRendererConfiguration")
    public Map<String, Map<String, String>> getViewRendererConfiguration() {
        return viewRendererConfiguration;
    }

    @JsonProperty("viewRendererConfiguration")
    public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
        ImmutableMap.Builder<String, Map<String, String>> builder = ImmutableMap.builder();
        for (Map.Entry<String, Map<String, String>> entry : viewRendererConfiguration.entrySet()) {
            builder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
        }
        this.viewRendererConfiguration = builder.build();
    }
}
