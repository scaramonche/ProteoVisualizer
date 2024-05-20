package dk.ku.cpr.proteoVisualizer.internal.model;

public enum NetworkType {
	FUNCTIONAL("full STRING network", "functional"),
	PHYSICAL("physical subnetwork", "physical");
	
	private final String name;
	private final String apiName;

	NetworkType(String name, String api) {
		this.name = name;
		this.apiName = api;
	}

	public String getAPIName() { return apiName; }
	
	public String toString() { return name; }
	
	public static NetworkType getType(String type) {
		for (NetworkType networkType : NetworkType.values()) {
			if (networkType.name.equalsIgnoreCase(type)) {
				return networkType;
			}
		}
		return null;
	}
}

