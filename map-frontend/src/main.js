import '@babel/polyfill'

import Vue from 'vue'
import App from './App.vue'
import { store } from "./store"
import VueResource from "vue-resource"
import router from './router'

Vue.config.productionTip = false;

Vue.use(VueResource);

new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app');
