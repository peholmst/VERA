import { registerMessageHandler } from "./channel";
import { Store } from "../store/Store";

export type User = {
    username: string;
    displayName: string;
    token?: string;
}

export type SessionInfo = {
    user?: User;
    active?: boolean;
};

export function registerSessionMessageHandler(store: Store<SessionInfo>): () => void {
    const unregisterMessageHandler = registerMessageHandler(msg => {
        switch (msg.type) {
            case "SESSION_INFO":
                store.update(state => {
                    state.user = {
                        username: msg.userName,
                        displayName: msg.displayName,
                        token: msg.token
                    };
                    state.active = true;
                });
                break;
            case "SESSION_CLOSED":
                store.update(state => { 
                    state.user = undefined;
                    state.active = false; 
                });
                break;
        }
    });
    return unregisterMessageHandler;
}
