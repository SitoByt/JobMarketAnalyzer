package network;

import java.util.Map;

public class ApiProvider {
	private String name;
    private String urlTemplate;
    private String appId;
    private String appKey;
    private Map<String, String> mapping;
    
	public String getName() {
		return name;
	}
	public String getUrlTemplate() {
		return urlTemplate;
	}
	public String getAppId() {
		return appId;
	}
	public String getAppKey() {
		return appKey;
	}
	public Map<String, String> getMapping() {
		return mapping;
	}

}
