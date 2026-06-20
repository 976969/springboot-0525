<template>
  <div class="banner-carousel" v-if="banners.length > 0">
    <el-carousel height="420px" :interval="4000" arrow="hover" indicator-position="outside">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <div class="banner-item" :style="{ backgroundImage: `url(${item.imageUrl})` }">
          <div class="banner-overlay">
            <div class="banner-text">
              <h3 class="banner-title">{{ item.title }}</h3>
              <p class="banner-content" v-if="item.content">{{ item.content }}</p>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const banners = ref([])

const loadBanners = async () => {
  try {
    const res = await request.get('/banner/active')
    banners.value = res.data || []
  } catch (e) {
    console.error('加载轮播图失败:', e)
  }
}

onMounted(loadBanners)
</script>

<style scoped>
.banner-carousel {
  margin-bottom: 20px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.banner-item {
  width: 100%;
  height: 100%;
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
  background-color: #f0f0f0;
  position: relative;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.75) 0%, rgba(0, 0, 0, 0.3) 60%, transparent 100%);
  padding: 30px 24px 16px;
}

.banner-text {
  color: #fff;
  text-align: left;
}

.banner-title {
  font-size: 22px;
  font-weight: bold;
  margin: 0 0 6px 0;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.5);
}

.banner-content {
  font-size: 14px;
  margin: 0;
  opacity: 0.9;
  line-height: 1.5;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}
</style>
