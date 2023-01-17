import { defineConfig } from 'vite';

export default defineConfig(({ command, mode }) => {
    if (command === 'serve') {
        return {
            define: {
                GLOBAL_API_BASE_URL: '"http://localhost/api/v1/game"',
                GLOBAL_EVENTS_BASE_URL: '"ws://localhost:8080/events/v1/game"',
                GLOBAL_FETCH_MODE: '"cors"',
                GLOBAL_FETCH_CREDENTIALS: '"omit"'
            },
            server: {
                host: true
            }
        }
    } else { // build
        return {
            define: {
                GLOBAL_API_BASE_URL: '"http://localhost/api/v1/game"',
                GLOBAL_EVENTS_BASE_URL: '"ws://localhost:8080/events/v1/game"',
                GLOBAL_FETCH_MODE: '"same-origin"',
                GLOBAL_FETCH_CREDENTIALS: '"same-origin"'
            }
        }
    }
})
