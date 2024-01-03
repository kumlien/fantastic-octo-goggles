import {useState} from "react";
import {MessageList, MessageListItem} from "@hilla/react-components/MessageList";
import {HorizontalLayout} from "@hilla/react-components/HorizontalLayout";
import {MessageInput} from "@hilla/react-components/MessageInput";
import {TheImageService} from "Frontend/generated/endpoints";
import {nanoid} from "nanoid";

const chatId = nanoid();

export default function ImageGenerationView() {
    const [messages, setMessages] = useState<MessageListItem[]>([]);
    const [imgSrc, setImgSrc] = useState('');

    async function generateImage(message: string) {
        setMessages(messages => [...messages, {
            text: message,
            userName: 'Prompt'
        }]);

        const response = await TheImageService.generateURI(message);
        setImgSrc(response);
        setMessages(messages => [...messages, {
            text: response,
            userName: 'URL'
        }]);
    }

    return (
      <div className="p-m flex flex-col h-full box-border">
            <HorizontalLayout id="image-layout" theme="spacing padding">
                <img id="the_image" src={imgSrc} width="30%"/>
            </HorizontalLayout>

          <MessageList items={messages} className="flex-grow"/>
          <MessageInput onSubmit={e => generateImage(e.detail.value)}/>
      </div>
    );
}
