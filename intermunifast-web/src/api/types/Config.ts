// Config Types - Based on ConfigDTOs.java

export interface ConfigResponse {
    id: number;
    key: string;
    value: string;
}

export interface CreateConfigRequest {
    key: string;
    value: string;
}

export interface UpdateConfigRequest {
    key?: string;
    value?: string;
}
