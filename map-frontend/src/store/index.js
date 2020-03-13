import Vue from "vue";
import Vuex from "vuex";
import {HnHMaxZoom} from "../utils/LeafletCustomTypes";


Vue.use(Vuex);

export const store = new Vuex.Store({
    state: {
        config: {
            MAP_ENDPOINT: "undefined"
        }
    },
    mutations: {
        setConfig(state, config) {
            state.config = config
        }

    },
    getters: {
        API_ENDPOINT: state => {
            return state.config.MAP_ENDPOINT + "/api"

        },
        TILE_ENDPOINT: state => {
            return state.config.MAP_ENDPOINT + "/"
        },
        getTileUrl: (state) => (x, y, zoom) => {
            return state.config.MAP_ENDPOINT + `/grids/${HnHMaxZoom - zoom}/${x}_${y}.png`

        }
    }


})