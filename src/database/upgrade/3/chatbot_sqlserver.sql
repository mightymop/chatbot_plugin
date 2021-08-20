UPDATE ofVersion SET version = 3 WHERE name = 'chatbot';

ALTER TABLE [ofChatbotRooms] ADD qarandom [int] NOT NULL DEFAULT -1; 

