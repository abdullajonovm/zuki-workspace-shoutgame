import ClientJs from 'clientjs';

export function getDeviceInfo(): DeviceInfo {
    const client = new ClientJs();
    const browser = client.getBrowser();
    const os = client.getOS();
    const engine = client.getEngine();

    return {
        osName: browser.name,
        osType: os.name,
        version: os.version,
        name: browser.version, // TODO: What should I set these values to in the browser?
        model: engine.name + engine.version
    };
}

export interface DeviceInfo {
    model: string;
    version: string;
    name: string;
    osName: string;
    osType: string;
}
