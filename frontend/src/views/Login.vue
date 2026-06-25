<!--
  登录页面 — 动漫风景背景
-->
<template>
  <div class="login-container">

    <el-card class="login-card">
      <div class="login-header">
        <div class="login-icon"><el-icon :size="36"><Monitor /></el-icon></div>
        <h2 class="login-title">智能实训评价系统</h2>
        <p class="login-subtitle">AI驱动的实训教学管理与评价平台</p>
      </div>
      <el-tabs v-model="activeTab" class="login-tabs" :stretch="true">
        <el-tab-pane name="account">
          <template #label><span class="tab-label"><el-icon><User /></el-icon> {{ loginTabLabel }}</span></template>
          <el-form :model="loginForm" :rules="loginRules" ref="loginRef" label-width="0" @keyup.enter="handleLogin">
            <el-form-item prop="role">
              <el-select v-model="loginForm.role" placeholder="请选择身份" style="width:100%" size="large">
                <el-option label="🧑‍💼  管理员" value="admin" />
                <el-option label="👨‍🏫  教师" value="teacher" />
                <el-option label="🎓  学生" value="student" />
              </el-select>
            </el-form-item>
            <template v-if="loginMode==='username'">
              <el-form-item prop="username"><el-input v-model="loginForm.username" prefix-icon="User" placeholder="请输入用户名" size="large" /></el-form-item>
              <el-form-item prop="password"><el-input v-model="loginForm.password" prefix-icon="Lock" type="password" placeholder="请输入密码" show-password size="large" /></el-form-item>
            </template>
            <template v-if="loginMode==='phone'">
              <el-form-item prop="phone"><el-input v-model="loginForm.phone" prefix-icon="Phone" placeholder="请输入手机号" size="large" /></el-form-item>
              <el-form-item prop="phoneCode"><div class="code-row"><el-input v-model="loginForm.phoneCode" prefix-icon="Key" placeholder="请输入短信验证码" size="large" class="code-input" /><el-button :disabled="phoneCodeDisabled" size="large" class="code-btn" @click="sendPhoneCode">{{ phoneCodeText }}</el-button></div></el-form-item>
            </template>
            <template v-if="loginMode==='email'">
              <el-form-item prop="email"><el-input v-model="loginForm.email" prefix-icon="Message" placeholder="请输入邮箱" size="large" /></el-form-item>
              <el-form-item prop="emailCode"><div class="code-row"><el-input v-model="loginForm.emailCode" prefix-icon="Key" placeholder="请输入验证码" size="large" class="code-input" /><el-button :disabled="codeDisabled" size="large" class="code-btn" @click="sendLoginCode">{{ codeText }}</el-button></div></el-form-item>
            </template>
            <el-form-item><el-button type="success" :loading="loading" style="width:100%" size="large" @click="handleLogin" round>{{ loginMode==='username'?'登 录':'验证并登录' }}</el-button></el-form-item>
          </el-form>
          <div class="switch-login-area">
            <div class="switch-divider"><span class="switch-text" @click="showOtherWays=!showOtherWays">换个方式登录 <el-icon class="switch-arrow" :class="{rotated:showOtherWays}"><ArrowDown /></el-icon></span></div>
            <transition name="slide-fade">
              <div v-if="showOtherWays" class="other-ways">
                <div class="way-card" :class="{active:loginMode==='phone'}" @click="switchLoginMode('phone')"><el-icon :size="22"><Phone /></el-icon><span>手机号登录</span></div>
                <div class="way-card" :class="{active:loginMode==='email'}" @click="switchLoginMode('email')"><el-icon :size="22"><Message /></el-icon><span>邮箱验证码登录</span></div>
                <div class="way-card" :class="{active:loginMode==='username'}" @click="switchLoginMode('username')"><el-icon :size="22"><User /></el-icon><span>账号密码登录</span></div>
              </div>
            </transition>
          </div>
          <div class="login-footer">还没有账号？<el-link type="primary" @click="activeTab='register'">立即注册</el-link></div>
        </el-tab-pane>
        <el-tab-pane name="register">
          <template #label><span class="tab-label"><el-icon><EditPen /></el-icon> 注册账号</span></template>
          <el-form :model="registerForm" :rules="registerRules" ref="registerRef" label-width="0">
            <el-form-item prop="username"><el-input v-model="registerForm.username" prefix-icon="User" placeholder="用户名（3-20字符）" size="large" /></el-form-item>
            <el-form-item prop="password"><el-input v-model="registerForm.password" prefix-icon="Lock" type="password" placeholder="密码（至少6位）" show-password size="large" /></el-form-item>
            <el-form-item prop="confirmPassword"><el-input v-model="registerForm.confirmPassword" prefix-icon="Lock" type="password" placeholder="确认密码" show-password size="large" /></el-form-item>
            <el-form-item prop="realName"><el-input v-model="registerForm.realName" prefix-icon="UserFilled" placeholder="真实姓名" size="large" /></el-form-item>
            <el-row :gutter="12"><el-col :span="12"><el-form-item prop="studentNo"><el-input v-model="registerForm.studentNo" placeholder="学号（选填）" size="large" /></el-form-item></el-col><el-col :span="12"><el-form-item prop="className"><el-input v-model="registerForm.className" placeholder="班级（选填）" size="large" /></el-form-item></el-col></el-row>
            <el-form-item prop="phone"><el-input v-model="registerForm.phone" prefix-icon="Phone" placeholder="手机号" size="large" /></el-form-item>
            <el-form-item prop="email"><el-input v-model="registerForm.email" prefix-icon="Message" placeholder="邮箱（接收验证码）" size="large" /></el-form-item>
            <el-form-item prop="code"><div class="code-row"><el-input v-model="registerForm.code" prefix-icon="Key" placeholder="邮箱验证码" size="large" class="code-input" /><el-button :disabled="regCodeDisabled" size="large" class="code-btn" @click="sendRegCode">{{ regCodeText }}</el-button></div></el-form-item>
            <el-form-item><el-button type="success" :loading="loading" style="width:100%" size="large" @click="handleRegister" round>注 册</el-button></el-form-item>
          </el-form>
          <div class="login-footer">已有账号？<el-link type="primary" @click="activeTab='account'">去登录</el-link></div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Monitor, User, EditPen, Phone, Message, ArrowDown, Lock, UserFilled, Key } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('account')
const loginMode = ref('username')
const showOtherWays = ref(false)
const loginTabLabel = computed(() => ({username:'账号登录',phone:'手机号登录',email:'邮箱验证码登录'}[loginMode.value]||'账号登录'))
const switchLoginMode = async (mode) => { loginMode.value = mode; await nextTick(); loginRef.value?.clearValidate() }

// 登录表单
const loginRef = ref(null)
const loginForm = reactive({ role:'', username:'', password:'', phone:'', phoneCode:'', email:'', emailCode:'' })
const loginRules = computed(() => {
  const b = {role:[{required:true,message:'请选择身份',trigger:'change'}]}
  if(loginMode.value==='username') return {...b,username:[{required:true,message:'请输入用户名',trigger:'blur'}],password:[{required:true,message:'请输入密码',trigger:'blur'}]}
  if(loginMode.value==='phone') return {...b,phone:[{required:true,message:'请输入手机号',trigger:'blur'},{pattern:/^1[3-9]\d{9}$/,message:'手机号格式不正确',trigger:'blur'}],phoneCode:[{required:true,message:'请输入验证码',trigger:'blur'}]}
  return {...b,email:[{required:true,message:'请输入邮箱',trigger:'blur'},{type:'email',message:'邮箱格式不正确',trigger:'blur'}],emailCode:[{required:true,message:'请输入验证码',trigger:'blur'}]}
})
const handleLogin = async () => {
  const v = await loginRef.value.validate().catch(()=>false); if(!v) return
  loading.value=true
  try {
    let r; const f=loginForm
    if(loginMode.value==='username') r=await request.post('/auth/login',{username:f.username,password:f.password,role:f.role})
    else if(loginMode.value==='phone') r=await request.post('/auth/login-by-phone',{phone:f.phone,code:f.phoneCode,role:f.role})
    else r=await request.post('/auth/login-by-email',{email:f.email,code:f.emailCode,role:f.role})
    loginSuccess(r.data)
  }catch(e){}finally{loading.value=false}
}

// 验证码
const codeDisabled=ref(false), codeText=ref('发送验证码'); let codeTimer
const sendLoginCode = async () => { try{await loginRef.value.validateField('email')}catch(e){return}; if(!loginForm.email)return; try{await request.post('/auth/send-code',{email:loginForm.email}); ElMessage.success('验证码已发送'); startCountdown('emailLogin')}catch(e){} }
const phoneCodeDisabled=ref(false), phoneCodeText=ref('发送验证码'); let phoneTimer
const sendPhoneCode = async () => { try{await loginRef.value.validateField('phone')}catch(e){return}; if(!loginForm.phone)return; try{await request.post('/auth/send-phone-code',{phone:loginForm.phone}); ElMessage.success('验证码已发送(控制台查看)'); startCountdown('phoneLogin')}catch(e){} }

// 注册
const registerRef=ref(null)
const registerForm=reactive({username:'',password:'',confirmPassword:'',realName:'',studentNo:'',className:'',phone:'',email:'',code:''})
const registerRules={
  username:[{required:true,message:'请输入用户名',trigger:'blur'},{min:3,max:20,message:'3-20个字符',trigger:'blur'}],
  password:[{required:true,message:'请输入密码',trigger:'blur'},{min:6,message:'至少6位',trigger:'blur'}],
  confirmPassword:[{required:true,message:'请确认密码',trigger:'blur'},{validator:(r,v,c)=>v!==registerForm.password?c(new Error('两次密码不一致')):c(),trigger:'blur'}],
  realName:[{required:true,message:'请输入真实姓名',trigger:'blur'}],
  phone:[{required:true,message:'请输入手机号',trigger:'blur'},{pattern:/^1[3-9]\d{9}$/,message:'格式不正确',trigger:'blur'}],
  email:[{required:true,message:'请输入邮箱',trigger:'blur'},{type:'email',message:'格式不正确',trigger:'blur'}],
  code:[{required:true,message:'请输入验证码',trigger:'blur'}]
}
const regCodeDisabled=ref(false), regCodeText=ref('发送验证码'); let regTimer
const handleRegister = async () => { const v=await registerRef.value.validate().catch(()=>false); if(!v)return; loading.value=true; try{const r=await request.post('/auth/register',{username:registerForm.username,password:registerForm.password,realName:registerForm.realName,email:registerForm.email,phone:registerForm.phone,studentNo:registerForm.studentNo,className:registerForm.className,code:registerForm.code}); ElMessage.success('注册成功'); loginSuccess(r.data)}catch(e){}finally{loading.value=false} }
const sendRegCode = async () => { try{await registerRef.value.validateField('email')}catch(e){return}; if(!registerForm.email)return; try{await request.post('/auth/send-code',{email:registerForm.email}); ElMessage.success('验证码已发送'); startCountdown('register')}catch(e){} }

const startCountdown = (t) => {
  if(t==='emailLogin') codeDisabled.value=true; else if(t==='phoneLogin') phoneCodeDisabled.value=true; else regCodeDisabled.value=true
  let c=60; const u=()=>{ if(t==='emailLogin') codeText.value=c+'s后重发'; else if(t==='phoneLogin') phoneCodeText.value=c+'s后重发'; else regCodeText.value=c+'s后重发'; c--; if(c<=0){ if(t==='emailLogin'){clearInterval(codeTimer);codeDisabled.value=false;codeText.value='发送验证码'}else if(t==='phoneLogin'){clearInterval(phoneTimer);phoneCodeDisabled.value=false;phoneCodeText.value='发送验证码'}else{clearInterval(regTimer);regCodeDisabled.value=false;regCodeText.value='发送验证码'}}}
  u(); const i=setInterval(u,1000)
  if(t==='emailLogin') codeTimer=i; else if(t==='phoneLogin') phoneTimer=i; else regTimer=i
}
const loginSuccess = (d) => { userStore.setToken(d.token); userStore.setUserInfo(d.user); ElMessage.success('登录成功'); router.push(d.user.role==='teacher'?'/teacher-home':'/home') }
</script>

<style scoped>
/* ========== 图片背景 + 动态叠加元素 ========== */
.login-container { display:flex; justify-content:flex-end; align-items:center; height:100vh;
  padding-right: 15%;
  background: url('/bg.png') center/cover no-repeat;
  position:relative; overflow:hidden; }
/* 卡片 */
.login-card{width:485px;padding:36px 32px 28px;border-radius:16px;background:rgba(255,255,255,0.93);
  display:flex;flex-direction:column;justify-content:center;
  box-shadow:0 16px 48px rgba(0,0,0,0.12),0 0 0 1px rgba(255,255,255,0.5);backdrop-filter:blur(10px);position:relative;z-index:1}
.login-header{text-align:center;margin-bottom:20px}
.login-icon{display:inline-flex;align-items:center;justify-content:center;width:60px;height:60px;border-radius:14px;
  background:linear-gradient(135deg,#a5d6a7,#66bb6a);color:#fff;margin-bottom:16px;box-shadow:0 4px 14px rgba(76,175,80,0.25)}
.login-icon .el-icon{font-size:30px}
.login-title{margin:0;font-size:29px;font-weight:700;color:#1a1a2e;letter-spacing:1px}
.login-subtitle{margin:8px 0 0;font-size:17px;color:#909399}
.login-tabs{margin-top:2px}
.tab-label{display:inline-flex;align-items:center;gap:4px;font-size:17px}
:deep(.el-tabs__item){font-size:18px!important;padding:0 18px!important;height:48px;line-height:48px}
:deep(.el-tabs__header){margin-bottom:12px}
:deep(.el-form-item){margin-bottom:22px}
.switch-login-area{margin-top:8px}
.switch-divider{text-align:center;margin:8px 0}
.switch-text{font-size:15px;color:#909399;cursor:pointer;user-select:none;transition:color .2s;display:inline-flex;align-items:center;gap:2px}
.switch-text:hover{color:#43a047}
.switch-arrow{font-size:12px;transition:transform .3s}
.switch-arrow.rotated{transform:rotate(180deg)}
.other-ways{display:flex;gap:10px;margin-top:10px}
.way-card{flex:1;display:flex;flex-direction:column;align-items:center;gap:6px;padding:14px 8px;border-radius:10px;
  border:1.5px solid #e4e7ed;cursor:pointer;transition:all .25s;font-size:14px;color:#606266}
.way-card:hover{border-color:#66bb6a;background:#e8f5e9;color:#43a047}
.way-card.active{border-color:#66bb6a;background:linear-gradient(135deg,#e8f5e9,#c8e6c9);color:#2e7d32;font-weight:600;box-shadow:0 2px 8px rgba(76,175,80,0.2)}
.code-row{display:flex;gap:10px}.code-input{flex:1}.code-btn{min-width:106px}
.login-footer{text-align:center;margin-top:12px;font-size:14px;color:#909399}
:deep(.el-input__inner){font-size:17px}
:deep(.el-button--success){background:#43a047;border-color:#43a047}
:deep(.el-button--success:hover){background:#388e3c;border-color:#388e3c}
:deep(.el-card__body){padding:0}
</style>
