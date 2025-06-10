export enum NotificationType {
  POST = 'POST',
  COMMENT = 'COMMENT',
  LIKE = 'LIKE',
  FOLLOW = 'FOLLOW',
  MENTION = 'MENTION',
  SYSTEM = 'SYSTEM'
}

export interface Notification {
  content: string;
  fromUser: string;
  type: NotificationType;
  confirmed: boolean;
  createdAt: string;
}

export interface NotificationCursor {
  id: string;
  createdAt: string;
}

export interface NotificationResponse {
  id: string;
  content: string;
  fromUser: string;
  type: NotificationType;
  confirmed: boolean;
  createdAt: string;
}

export interface CursorResponse<T, C> {
  items: T[];
  nextCursor: C | null;
  hasNext: boolean;
}

export interface UnconfirmedNotificationsCountResponse {
  count: number;
}
