export type ConnectionStatus = "null" | "PENDING" | "ACCEPT" | "REJECT" | "CANCEL";

export interface ConnectionStatusResponse {
  requesterName: string;
  respondentName: string;
  status: ConnectionStatus;
}

export interface ConnectionCodeResponse {
  code: string;
}

export interface ConnectionRequestResponse {
  status: ConnectionStatus;
  requesterName: string;
  respondentName: string;
}

export interface WebSocketMessage {
  type: 'CONNECTION_REQUEST' | 'CONNECTION_ACCEPT' | 'CONNECTION_REJECT' | 'CONNECTION_CANCEL';
  data: {
    requesterName?: string;
    respondentName?: string;
    status: ConnectionStatus;
  };
} 