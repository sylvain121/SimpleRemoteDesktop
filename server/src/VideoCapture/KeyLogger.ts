export class KeyLogger {
    private keysArray: number[] = [];

    public keyDown(keysym: number) {
        this.keysArray.push(keysym);
    }

    public get keys() {
        return this.keysArray;
    }
}
