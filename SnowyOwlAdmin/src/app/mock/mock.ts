export class Mock {
    mockRequests: MockRequest[];

    constructor(requests: MockRequest[]) {

        this.mockRequests = requests;
    }

    canHandleRequest(request: string): boolean {
        for (const mockRequest of this.mockRequests) {
            if (mockRequest.match.test(request))
                return true;
        }

        return false;
    }

    handleRequest(request: string, requestData: any): any {
        for (const mockRequest of this.mockRequests) {

            if (mockRequest.match.test(request)) {
                return mockRequest.loadData ? mockRequest.loadData(requestData) : mockRequest.data;
            }

        }
    }

}

export interface MockRequest {
    match: RegExp;
    data?: any;
    loadData?: (data: any) => any
}
