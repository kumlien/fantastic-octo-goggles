import ChatView from 'Frontend/views/chat/ChatView';
import MainLayout from 'Frontend/views/MainLayout.js';
import { lazy } from 'react';
import { createBrowserRouter, RouteObject } from 'react-router-dom';
import StreamingChatView from "Frontend/views/streaming-chat/StreamingChatView";
import ImageGenerationView from "Frontend/views/image-generation/ImageGenerationView";


export const routes: RouteObject[] = [
  {
    element: <MainLayout />,
    handle: { title: 'Main' },
    children: [
      { path: '/', element: <ChatView />, handle: { title: 'Chat' } },
      { path: '/streaming', element: <StreamingChatView />, handle: { title: 'Streaming Chat' } },
      { path: '/image-generation', element: <ImageGenerationView />, handle: { title: 'Image Generation' } },
    ],
  },
];

export default createBrowserRouter(routes);
