import {createRouter,createWebHistory} from 'vue-router';
import Dashboard from '../views/Dashboard.vue';
import StockPool from '../views/StockPool.vue';
import Strategy from '../views/Strategy.vue';
import Backtest from '../views/Backtest.vue';
import Signals from '../views/Signals.vue';
import Positions from '../views/Positions.vue';
export default createRouter({history:createWebHistory(),routes:[{path:'/',component:Dashboard},{path:'/stocks',component:StockPool},{path:'/strategies',component:Strategy},{path:'/backtest',component:Backtest},{path:'/signals',component:Signals},{path:'/positions',component:Positions}]});
