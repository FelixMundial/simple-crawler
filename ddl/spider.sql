CREATE TABLE IF NOT EXISTS `spider`.`bilibili_hot`
(
    `id`             INT(16) AUTO_INCREMENT,
    `ranking_number` VARCHAR(3)              NOT NULL COMMENT '榜单名次',
    `v_image_url`    VARCHAR(128)            NULL COMMENT '视频图片地址',
    `v_title`        VARCHAR(64)             NOT NULL COMMENT '视频标题',
    `v_bv`           VARCHAR(12)             NOT NULL COMMENT '视频BV号',
    `v_play_count`   VARCHAR(10) DEFAULT '0' NOT NULL COMMENT '视频播放次数',
    `v_view_count`   VARCHAR(10) DEFAULT '0' NOT NULL COMMENT '视频弹幕数量',
    `v_author`       VARCHAR(32)             NULL COMMENT '视频作者',
    `v_author_url`   VARCHAR(30)             NULL COMMENT '视频作者空间地址',
    `v_points`       VARCHAR(12) DEFAULT '0' NOT NULL COMMENT '视频综合得分',
    `v_desc`         VARCHAR(256)            NULL COMMENT '视频简介',
    `update_time`    DATETIME                NOT NULL,
    CONSTRAINT `bilibili_hot_id_uindex`
        UNIQUE (`id`)
)
    COMMENT 'b站热门视频排行榜（全站榜）' CHARSET = `utf8mb4`;

ALTER TABLE `spider`.`bilibili_hot`
    ADD PRIMARY KEY (`id`);

CREATE TABLE IF NOT EXISTS `spider`.`proxy_ip`
(
    `id`              INT(16) AUTO_INCREMENT,
    `ip`              VARCHAR(16) NULL COMMENT 'IP地址',
    `ip_port`         VARCHAR(5)  NULL COMMENT 'IP端口号',
    `addr`            VARCHAR(32) NULL COMMENT '服务器地址',
    `anonymity`       VARCHAR(8)  NULL COMMENT '匿名性（1：高匿；0：非匿）',
    `type`            TINYINT     NULL COMMENT '类型（1：HTTP，0：HTTPS）',
    `speed`           FLOAT       NULL COMMENT '速度（单位：秒）',
    `conn_time`       FLOAT       NULL COMMENT '连接时间（单位：秒）',
    `surviving_time`  BIGINT      NULL COMMENT '存活时间（单位：小时）',
    `validation_time` DATETIME    NULL COMMENT '验证时间',
    CONSTRAINT `proxy_ip_id_uindex`
        UNIQUE (`id`)
)
    COMMENT 'IP代理池' CHARSET = `utf8mb4`;

ALTER TABLE `spider`.`proxy_ip`
    ADD PRIMARY KEY (`id`);

CREATE TABLE IF NOT EXISTS `spider`.`zhihu_hot`
(
    `id`              INT(16) AUTO_INCREMENT,
    `ranking_number`  VARCHAR(3)              NOT NULL COMMENT '榜单名次',
    `q_image_url`     VARCHAR(128)            NULL COMMENT '话题图片地址',
    `q_title`         VARCHAR(64)             NOT NULL COMMENT '话题标题',
    `q_id`            VARCHAR(12)             NOT NULL COMMENT '话题对应提问号',
    `q_metrics`       VARCHAR(12) DEFAULT '0' NOT NULL COMMENT '话题热度',
    `q_excerpt`       LONGTEXT                NULL COMMENT '话题摘要',
    `q_answers_count` INT(6)                  NULL COMMENT '话题回答数',
    `update_time`     DATETIME                NOT NULL,
    CONSTRAINT `zhihu_hot_id_uindex`
        UNIQUE (`id`)
)
    COMMENT '知乎热榜' CHARSET = `utf8mb4`;

ALTER TABLE `spider`.`zhihu_hot`
    ADD PRIMARY KEY (`id`);

CREATE TABLE `baidu_hot`
(
    `id`             INT(16) AUTO_INCREMENT,
    `ranking_number` VARCHAR(3)              NOT NULL COMMENT '榜单名次',
    `i_keyword`      VARCHAR(32)             NOT NULL COMMENT '热点关键词',
    `i_title`        VARCHAR(64)             NULL COMMENT '热点标题',
    `i_text`         MEDIUMTEXT              NULL COMMENT '热点摘要',
    `i_news_url`     VARCHAR(256)            NULL COMMENT '热点链接新闻url',
    `i_video_url`    VARCHAR(256)            NULL COMMENT '热点链接视频url',
    `i_picture_url`  VARCHAR(256)            NULL COMMENT '热点链接图片url',
    `i_metrics`      VARCHAR(12) DEFAULT '0' NOT NULL COMMENT '搜索指数',
    `update_time`    DATETIME                NOT NULL,
    CONSTRAINT `baidu_hot_id_uindex`
        UNIQUE (`id`)
)
    COMMENT '百度实时热点排行榜' CHARSET = `utf8mb4`;

ALTER TABLE `baidu_hot`
    ADD PRIMARY KEY (`id`);

