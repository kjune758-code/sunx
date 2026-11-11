<script setup>
import {ref,onMounted,computed} from 'vue';import {api} from '../api';
const props=defineProps(['endpoint']);const data=ref(null),error=ref('');
const columns=computed(()=>Array.isArray(data.value)&&data.value.length?Object.keys(data.value[0]):[]);
onMounted(async()=>{try{data.value=await api(props.endpoint)}catch(e){error.value=e.message}});
</script><template><p v-if="error" role="alert">{{error}}</p><p v-else-if="data===null">加载中…</p><template v-else-if="Array.isArray(data)"><p v-if="!data.length">暂无数据。确认信号不会创建持仓或订单。</p><div v-else class="table"><table><thead><tr><th v-for="c in columns" :key="c">{{c}}</th></tr></thead><tbody><tr v-for="(row,i) in data" :key="i"><td v-for="c in columns" :key="c">{{row[c]}}</td></tr></tbody></table></div></template><div v-else class="cards"><article v-for="(value,key) in data" :key="key"><small>{{key}}</small><p>{{value}}</p></article></div></template>
