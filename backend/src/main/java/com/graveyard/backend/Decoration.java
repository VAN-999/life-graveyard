package com.graveyard.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "decorations")
public class Decoration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String description;

    @Column(nullable = false, length = 20)
    private String category;  // TOMBSTONE, OFFERING, PET, EFFECT

    @Column(name = "price", nullable = false)
    private Integer price;  // 冥币价格

    @Column(name = "icon", length = 100)
    private String icon;  // 前端用的图标名

    @Column(name = "preview_url", length = 255)
    private String previewUrl;  // 预览图链接

    @Column(name = "is_default")
    private Boolean isDefault = false;  // 是否默认赠送

    public Decoration() {}

    public Decoration(String name, String description, String category, Integer price, String icon) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.icon = icon;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}