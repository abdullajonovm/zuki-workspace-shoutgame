// import { MockBackend, MockConnection } from '@angular/http/testing';
// import { BaseRequestOptions, ConnectionBackend, Http, RequestOptions, Response, ResponseOptions } from '@angular/http';
// import { Mock } from './mock';
// import { SUBSCRIBER_SERVICE_MOCK } from 'app/mock/subscriber-service.mock';
// import { NgModule } from '@angular/core';
// import * as uuid from 'uuid/v4';
// import { GAME_SERVICE_MOCK } from 'app/mock/game-service.mock';
// import { CATEGORY_SERVICE_MOCK } from 'app/mock/category-service.mock';
//
// export function httpFactory(backend: ConnectionBackend, options: RequestOptions) {
//     return new Http(backend, options);
// }
//
// @NgModule({
//     providers: [
//         MockBackend,
//         BaseRequestOptions,
//         {
//             provide: Http,
//             useFactory: httpFactory,
//             deps: [MockBackend, BaseRequestOptions]
//         }
//     ]
// })
// export class MockModule {
//     constructor(mockBackend: MockBackend) {
//         const mocks: Mock[] = [SUBSCRIBER_SERVICE_MOCK, GAME_SERVICE_MOCK, CATEGORY_SERVICE_MOCK];
//         const tickets: { [key: string]: any } = {};
//         const ticketKeys: string[] = [];
//
//         mockBackend.connections.subscribe((con: MockConnection) => {
//             const requestUrl = con.request.url;
//             for (const key of ticketKeys) {
//                 if (requestUrl.indexOf(key) !== -1) {
//                     sendResponse(tickets[key], con);
//                     return;
//                 }
//             }
//             const requestData = getJsonFromFormParams(con.request.getBody());
//
//             // When a HTTP request is made, look in all of our mocks requests to return a value.
//             const mock = findMatchingMock(requestUrl, mocks);
//             if (mock) {
//                 const data = mock.handleRequest(requestUrl, requestData);
//                 const ticket = uuid();
//                 tickets[ticket] = data;
//                 ticketKeys.push(ticket);
//                 sendTicket(ticket, con);
//             } else {
//                 throw new Error('No mocks found for: ' + requestUrl);
//             }
//         });
//     }
// }
//
// function findMatchingMock(requestUrl: string, mocks: Mock[]): Mock | null {
//     for (const mock of mocks) {
//         if (mock.canHandleRequest(requestUrl))
//             return mock;
//     }
//     return null;
// }
//
// function sendResponse(responseBody: any, connection: MockConnection) {
//     const responseOptions = new ResponseOptions({body: responseBody});
//     const response: Response = new Response(responseOptions);
//     connection.mockRespond(response);
// }
//
// function sendTicket(ticket: string, connection: MockConnection) {
//     const responseOptions = new ResponseOptions({
//         body: {
//             ticket,
//             estimatedWaitTime: 300
//         }
//     });
//     const response = new Response(responseOptions);
//     connection.mockRespond(response);
// }
//
// function getJsonFromFormParams(query: string): {} {
//     return query ? JSON.parse('{"' + query.replace(/&/g, '","').replace(/=/g, '":"') + '"}',
//         function (key, value) {
//             return key === '' ? value : decodeURIComponent(value);
//         }) : {};
// }
