INSERT INTO ofVersion (name, version) VALUES ('chatbot', 1) ON DUPLICATE KEY UPDATE version '1';

CREATE TABLE IF NOT EXISTS `ofChatbotRooms` (
  `roomjid` varchar(255) NOT NULL,
  `qa_online` int NOT NULL,
  `qa_db` int NOT NULL,
   UNIQUE KEY unique_uid (`roomjid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `ofChatbotRooms` ADD PRIMARY KEY (`roomjid`);

CREATE TABLE IF NOT EXISTS `ofChatbotQA` (
  `id` varchar(130) NOT NULL,
  `q` varchar(255) NOT NULL,
  `a` text,
   UNIQUE KEY unique_uid (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `ofChatbotQA` ADD PRIMARY KEY (`id`);
