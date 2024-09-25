import {JsonPatchItem, JsonPatchOps} from './api';
import { Configuration, BookControllerApi } from './api';

const patch: JsonPatchItem[] = [
    {
        op: JsonPatchOps.Replace,
        value: 'New Title',
        path: '/title',
    },
    {
        op: JsonPatchOps.Replace,
        value: 1,
        path: '/author/id',
    }];

const configuration = new Configuration({
    basePath: "http://localhost:8083",
});

const controllerApi = new BookControllerApi(configuration);

const options = {
    headers: {
        Authorization: 'Bearer YOUR_TOKEN_HERE'
    },
};

controllerApi.updateBook(1, patch, options)
    .then((response) => {
        console.log(response.data);
    })
    .catch((error) => {
        console.error(error);
    });