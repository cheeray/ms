package com.cheeray.ms;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Consul registration info.
 * @author Chengwei.yan
 */
public class RegisterInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String hostName;

	private String instanceGroup;

	private String instanceId;

	private String defaultQueryTag;

	private String serviceName;

	private Map<String, String> serverListQueryTags;

	private List<String> tags;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getInstanceGroup() {
		return instanceGroup;
	}

	public void setInstanceGroup(String instanceGroup) {
		this.instanceGroup = instanceGroup;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getDefaultQueryTag() {
		return defaultQueryTag;
	}

	public void setDefaultQueryTag(String defaultQueryTag) {
		this.defaultQueryTag = defaultQueryTag;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Map<String, String> getServerListQueryTags() {
		return serverListQueryTags;
	}

	public void setServerListQueryTags(Map<String, String> serverListQueryTags) {
		this.serverListQueryTags = serverListQueryTags;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

}
