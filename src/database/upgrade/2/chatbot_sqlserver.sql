UPDATE ofVersion SET version = 2 WHERE name = 'chatbot';

ALTER TABLE [ofChatbotRooms] ADD qaaiml [int] NOT NULL DEFAULT 0; 

