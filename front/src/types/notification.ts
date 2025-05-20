export enum NotificationType {
  POST = 'POST',
  COMMENT = 'COMMENT',
  LIKE = 'LIKE',
  COUPLE = 'COUPLE'
}

export interface Notification {
  content: string;
  fromUser: string;
  type: NotificationType;
  confirmed: boolean;
  createdAt: string;
}

export interface MainNotificationResponse {
  count: number;
  notifications: Notification[];
} 