<script setup>
import {ref,onMounted} from 'vue';import {api,post} from '../api';
const rows=ref([]),symbol=ref('AAPL'),strategy=ref('breakout'),quantity=ref(10),error=ref(''),busy=ref(false);
async function load(){rows.value=await api('/signals')}
async function act(fn){busy.value=true;error.value='';try{await fn();await load()}catch(e){error.value=e.message}finally{busy.value=false}}
function generate(){act(()=>post('/signals',{symbol:symbol.value,strategy:strategy.value,quantity:quantity.value}))}
function confirmSignal(id){if(window.confirm('确认已人工审核？仅记录确认状态，不下单。'))act(()=>post(`/signals/${id}/confirm`,{}))}
function fillSignal(id){if(window.confirm('按信号价格和数量登记模拟买入？'))act(()=>post(`/signals/${id}/fill`,{}))}
function skipSignal(id){act(()=>post(`/signals/${id}/skip`,{}))}
onMounted(()=>act(async()=>{}));
</script><template><h1>信号审核</h1><p>单笔上限：模拟账户 100000 报价货币的 10%；HOLD 自动阻断。确认有效期 24 小时。</p><select aria-label="标的" v-model="symbol"><option>AAPL</option><option>MSFT</option><option>00700.HK</option></select><select aria-label="策略" v-model="strategy"><option>breakout</option><option>mean_reversion</option></select><input aria-label="数量" v-model.number="quantity" type="number" min="1" max="100000"><button :disabled="busy" @click="generate">生成模拟信号</button><p role="alert">{{error}}</p><div class="table"><table><thead><tr><th>ID</th><th>标的</th><th>方向</th><th>价格 / 数量</th><th>状态</th><th>风控</th><th>人工审核</th></tr></thead><tbody><tr v-for="s in rows" :key="s.id"><td>{{s.id}}</td><td>{{s.symbol}}</td><td>{{s.side}}</td><td>{{s.price}} / {{s.quantity}}</td><td>{{s.status}}</td><td>{{s.riskMessage}}</td><td><button :disabled="busy||s.status!=='PENDING'" @click="confirmSignal(s.id)">仅审核</button> <button :disabled="busy||s.status!=='PENDING'" @click="fillSignal(s.id)">模拟成交</button> <button :disabled="busy||s.status!=='PENDING'" @click="skipSignal(s.id)">跳过</button></td></tr></tbody></table><p v-if="!rows.length">暂无信号，请先生成。</p></div></template>
