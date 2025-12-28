export class Store<T extends {}> {

    private state: T;
    private subscribers = new Set<(state: Readonly<T>) => void>();
    private batching = false;
    private pendingNotify = false;

    constructor(initialState: T) {
        this.state = initialState;
    }

    get snapshot(): Readonly<T> {
        return this.state;
    }

    update(mutator: (state: T) => void): void {
        mutator(this.state);
        this.notify();
    }

    private notify() {
        if (this.batching) {
            this.pendingNotify = true;
            return;
        }
        for (const fn of this.subscribers) {
            fn(this.state);
        }
    }

    batch(fn: () => void): void {
        this.batching = true;
        try {
            fn();
        } finally {
            this.batching = false;
            if (this.pendingNotify) {
                this.pendingNotify = false;
                this.notify();
            }
        }
    }

    subscribe(fn: (state: Readonly<T>) => void): () => void {
        this.subscribers.add(fn);
        // Call function to sync it the current state
        fn(this.state);
        return () => this.subscribers.delete(fn);
    }
}