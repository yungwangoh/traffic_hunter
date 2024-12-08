package ygo.traffichunter.agent.engine.env.yaml.root;

import ygo.traffichunter.agent.engine.env.yaml.root.agent.AgentSubProperty;

/**
 * @author yungwang-o
 * @version 1.0.0
 */
public class RootYamlProperty {

    private AgentSubProperty agent;

    public RootYamlProperty() {
    }

    public RootYamlProperty(final AgentSubProperty agent) {
        this.agent = agent;
    }

    public AgentSubProperty getAgent() {
        return agent;
    }

    public void setAgent(final AgentSubProperty agent) {
        this.agent = agent;
    }
}
