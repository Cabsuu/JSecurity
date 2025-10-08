package com.jerae.jsecurity.models;

import java.util.List;
import java.util.UUID;

public class PlayerData {
    private int id;
    private UUID uuid;
    private String name;
    private List<String> ips;
    private String joined;

    public PlayerData(int id, UUID uuid, String name, List<String> ips, String joined) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.ips = ips;
        this.joined = joined;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public List<String> getIps() {
        return ips;
    }

    public String getJoined() {
        return joined;
    }

    public void addIp(String ip) {
        if (!ips.contains(ip)) {
            ips.add(ip);
        }
    }
}