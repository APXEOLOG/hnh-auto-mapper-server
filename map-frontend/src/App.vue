<template>
    <div id="app">
        <LoginView v-if="authToken === false" v-on:authenticated="authToken = $event"></LoginView>
        <router-view v-else v-on:error="authToken = false"/>
    </div>
</template>

<script>
    import 'bootstrap/dist/css/bootstrap.min.css'

    import Vue from 'vue'

    import LoginView from "./components/LoginView";
    import axios from 'axios';

    export default {
        name: "App",
        components: {LoginView},
        data: function () {
            return {
                authenticated: false,
                authToken: false
            }
        },
        mounted: function () {
            axios.get("/config.json").then(resp => {
                this.$store.commit("setConfig", resp.data)
                if (localStorage.authToken) {
                    this.authToken = localStorage.authToken;
                }
            })
        },
        watch: {
            authToken(value) {
                if(value) {
                    localStorage.authToken = value;
                    Vue.http.headers.common["Authorization"] = "Basic " + value;
                }
            }
        }
    }
</script>

<style scoped>
</style>