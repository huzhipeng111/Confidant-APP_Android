package com.stratagile.pnrouter.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class ActiveEntity {
    @Id(autoincrement = true)
    private Long id;
    private String activeId;
    @Generated(hash = 1200970728)
    public ActiveEntity(Long id, String activeId) {
        this.id = id;
        this.activeId = activeId;
    }

    public ActiveEntity(String activeId) {
        this.activeId = activeId;
    }

    @Generated(hash = 19531692)
    public ActiveEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getActiveId() {
        return this.activeId;
    }
    public void setActiveId(String activeId) {
        this.activeId = activeId;
    }
}
