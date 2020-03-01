import '@babel/polyfill'

import Vue from 'vue'
import App from './App.vue'
import VueResource from "vue-resource"
import router from './router'
import {HnHMaxZoom} from "./utils/LeafletCustomTypes";
import {MAP_ENDPOINT} from "./config";


export const API_ENDPOINT = `${MAP_ENDPOINT}/api`;
export const TILE_ENDPOINT = `${MAP_ENDPOINT}/`;

export function getTileUrl(x, y, zoom) {
    return `${TILE_ENDPOINT}/grids/${HnHMaxZoom - zoom}/${x}_${y}.png`
}

Vue.config.productionTip = false;

Vue.use(VueResource);

new Vue({
    router,
    render: h => h(App)
}).$mount('#app');
