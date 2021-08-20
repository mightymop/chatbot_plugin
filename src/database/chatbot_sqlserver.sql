BEGIN TRAN 
	IF EXISTS (SELECT [name] FROM ofVersion WHERE [name] = 'chatbot') 
		BEGIN 
			UPDATE ofVersion SET [version] = '1' WHERE [name] = 'chatbot' 
		END 		
		ELSE 
		BEGIN 
			INSERT INTO ofVersion (name, version) VALUES ('chatbot', 1)
		END 
COMMIT TRAN
GO

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ofChatbotRooms' AND xtype='U')
   CREATE TABLE  [dbo].[ofChatbotRooms](
	[roomjid] [varchar](255) NOT NULL,
	[qa_online] [int] NOT NULL,
	[qa_db] [int] NOT NULL,
 CONSTRAINT [PK_ofChatbotRooms] PRIMARY KEY (roomjid) 
)
GO

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ofChatbotQA' AND xtype='U')
   CREATE TABLE  [dbo].[ofChatbotQA](
	[id] [varchar](130) NOT NULL,
	[q] [varchar](255) NOT NULL,
	[a] [text] NOT NULL,	
 CONSTRAINT [PK_ofChatbotQA] PRIMARY KEY CLUSTERED  (id) 
)
GO
