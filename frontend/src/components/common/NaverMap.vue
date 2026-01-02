<template>
  <div class="relative w-full h-full">
    <div ref="mapContainer" class="w-full h-full rounded-xl bg-gray-800"></div>
    
    <!-- 지도 검색 오버레이 -->
    <div class="absolute top-2 right-2 z-10 flex gap-2 w-80">
      <select 
        v-model="searchType" 
        class="bg-white/90 backdrop-blur text-sm border border-gray-300 rounded-lg px-2 py-2 focus:outline-none focus:ring-2 focus:ring-primary text-gray-700 font-bold"
      >
        <option value="12">관광지</option>
        <option value="32">숙소</option>
        <option value="39">맛집</option>
      </select>
      <input 
        v-model="searchKeyword" 
        @keyup.enter="performSearch"
        type="text" 
        placeholder="장소 검색 (Enter)" 
        class="flex-1 px-3 py-2 rounded-lg bg-white/90 backdrop-blur text-sm border border-gray-300 shadow-md focus:outline-none focus:ring-2 focus:ring-primary text-black"
      />
      <button @click="performSearch" class="px-3 py-2 bg-primary text-white rounded-lg shadow-md hover:bg-blue-600 transition-colors">
        <span class="material-symbols-outlined text-sm">search</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch, ref } from 'vue';
import planApi from '@/services/api/plan';

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['add-item']);

const mapContainer = ref(null);
const searchKeyword = ref('');
const searchType = ref('12'); // 기본: 관광지
let map = null;
let markers = [];
let searchMarkers = []; // 검색 결과 마커
let polyline = null;
let searchInfowindow = null;

const initMap = () => {
  if (!mapContainer.value) return;

  if (!window.naver || !window.naver.maps) {
    console.error('Naver Maps script not loaded');
    return;
  }

  const mapOptions = {
    center: new window.naver.maps.LatLng(36.2683, 127.6358),
    zoom: 7,
  };

  map = new window.naver.maps.Map(mapContainer.value, mapOptions);
  
  // 검색 결과용 인포윈도우 초기화
  searchInfowindow = new window.naver.maps.InfoWindow({
      disableAnchor: false,
      borderWidth: 0,
      backgroundColor: "transparent",
      pixelOffset: new window.naver.maps.Point(0, -10)
  });

  updateMap();
};

const performSearch = async () => {
  if (!searchKeyword.value.trim()) return;
  
  try {
    const result = await planApi.searchTour({ 
        keyword: searchKeyword.value.trim(), 
        contentTypeId: searchType.value,
        pageNo: 1, 
        numOfRows: 20 
    });
    const items = result.response?.body?.items?.item || [];
    
    if (items.length === 0) {
      alert('검색 결과가 없습니다.');
      return;
    }
    
    // 기존 검색 마커 제거
    searchMarkers.forEach(m => m.setMap(null));
    searchMarkers = [];
    if (searchInfowindow) searchInfowindow.close();

    const bounds = new window.naver.maps.LatLngBounds();

    items.forEach(item => {
      const lat = Number(item.mapy);
      const lng = Number(item.mapx);
      if (!lat || !lng) return;

      const position = new window.naver.maps.LatLng(lat, lng);
      bounds.extend(position);

      const marker = new window.naver.maps.Marker({
        position: position,
        map: map,
        title: item.title,
        icon: {
            content: `
              <div style="
                width: 12px; height: 12px;
                background-color: #9ca3af;
                border: 2px solid white;
                border: 2px solid ${searchType.value === '32' ? '#a855f7' : '#9ca3af'};
                background-color: ${searchType.value === '32' ? '#d8b4fe' : '#9ca3af'};
                border-radius: 50%;
                box-shadow: 0 2px 4px rgba(0,0,0,0.3);
              "></div>
            `,
            anchor: new window.naver.maps.Point(6, 6)
        }
      });
      
      // 클릭 시 '추가하기' 버튼이 있는 인포윈도우 표시
      window.naver.maps.Event.addListener(marker, 'click', () => {
        const content = `
          <div style="background: white; padding: 10px; border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); border: 1px solid #e5e7eb; min-width: 150px;">
            <h3 style="font-weight: bold; margin-bottom: 4px; color: #1f2937;">${item.title}</h3>
            <p style="font-size: 11px; color: #6b7280; margin-bottom: 8px;">${item.addr1 || ''}</p>
            <button id="add-btn-${item.contentid}" style="
              width: 100%;
              padding: 6px;
              background-color: #3b82f6;
              color: white;
              border: none;
              border-radius: 4px;
              font-size: 12px;
              font-weight: bold;
              cursor: pointer;
            ">일정에 추가 +</button>
          </div>
        `;
        
        searchInfowindow.setContent(content);
        searchInfowindow.open(map, marker);
        
        // DOM 추가 후 이벤트 리스너 바인딩 (setTimeout으로 렌더링 대기)
        setTimeout(() => {
          const btn = document.getElementById(`add-btn-${item.contentid}`);
          if (btn) {
            btn.onclick = () => {
              emit('add-item', item);
              searchInfowindow.close();
              alert(`${item.title} 추가됨!`);
            };
          }
        }, 100);
      });

      searchMarkers.push(marker);
    });

    map.fitBounds(bounds, { margin: 50 });

  } catch (error) {
    console.error('Search failed:', error);
    alert('장소 검색 중 오류가 발생했습니다.');
  }
};

const updateMap = () => {
  if (!map) return;

  // 1. 기존 마커 및 라인 제거
  markers.forEach((marker) => marker.setMap(null));
  markers = [];
  if (polyline) {
    polyline.setMap(null);
    polyline = null;
  }

  if (!props.items || props.items.length === 0) return;

  const points = [];
  // Bounds는 Polyline 경로를 통해 계산하거나 직접 확장
  let minLat = 90, maxLat = -90, minLng = 180, maxLng = -180;

  let markerIndex = 0;

  // 2. 마커 생성
  props.items.forEach((item, index) => {
    // 교통편은 지도에 표시하지 않음
    if (item.type === 'transport') {
       return; 
    }

    const d = item.data;
    
    // 1. 최상위 레벨 확인 (camelCase, snake_case, Lowercase)
    let lat = Number(d.mapy || d.mapY || d.map_y); 
    let lng = Number(d.mapx || d.mapX || d.map_x);
    
    // 2. tourInfo 내부 확인
    if ((!lat || isNaN(lat)) && d.tourInfo) {
         lat = Number(d.tourInfo.mapy || d.tourInfo.mapY || d.tourInfo.map_y);
         lng = Number(d.tourInfo.mapx || d.tourInfo.mapX || d.tourInfo.map_x);
    }
    
    // 좌표 없음
    if (!lat || !lng || isNaN(lat) || isNaN(lng)) return;

    // 유효한 마커이므로 인덱스 증가
    markerIndex++;
    const isStart = markerIndex === 1;

    const position = new window.naver.maps.LatLng(lat, lng);
    points.push(position);
    
    // Bounds 값 갱신
    if (lat < minLat) minLat = lat;
    if (lat > maxLat) maxLat = lat;
    if (lng < minLng) minLng = lng;
    if (lng > maxLng) maxLng = lng;

    const title = d.title || d.tourInfo?.title || '여행지';

    // 커스텀 마커 스타일 (Tailwind 색상 참조: Red-500 #ef4444, Blue-500 #3b82f6)
    const markerColor = isStart ? '#ef4444' : '#3b82f6';
    const markerContent = `
        <div style="
            background-color: ${markerColor};
            width: 24px; height: 24px;
            border-radius: 50%;
            border: 2px solid white;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            font-size: 12px;
        ">
            ${markerIndex}
        </div>
    `;

    // 마커 생성
    const marker = new window.naver.maps.Marker({
        position: position,
        map: map,
        title: title,
        icon: {
            content: markerContent,
            size: new window.naver.maps.Size(24, 24),
            anchor: new window.naver.maps.Point(12, 12)
        }
    });
    
    // 인포윈도우 (선택 사항 - 마커 클릭 시 표시 등)
    const contentString = `<div style="padding:5px;font-size:12px;color:black;">${markerIndex}. ${title}</div>`;
    
    const infowindow = new window.naver.maps.InfoWindow({
        content: contentString,
        disableAnchor: false,
        borderWidth: 1,
        backgroundColor: "white",
        borderColor: "#ccc",
        pixelOffset: new window.naver.maps.Point(0, -10)
    });

    window.naver.maps.Event.addListener(marker, "mouseover", function(e) {
        if (infowindow.getMap()) {
            infowindow.close();
        } else {
            infowindow.open(map, marker);
        }
    });
    
    window.naver.maps.Event.addListener(marker, "mouseout", function(e) {
        infowindow.close();
    });

    markers.push(marker);
  });

  // 3. 경로 그리기 (Polyline)
  if (points.length > 1) {
    polyline = new window.naver.maps.Polyline({
      map: map,
      path: points,
      strokeColor: '#3b82f6', // Primary Blue
      strokeWeight: 4,
      strokeOpacity: 0.8,
      strokeStyle: 'solid',
      strokeLineCap: 'round',
      strokeLineJoin: 'round'
    });
  }

  // 4. 지도 범위 재설정
  if (points.length > 0) {
    const bounds = new window.naver.maps.LatLngBounds(
        new window.naver.maps.LatLng(minLat, minLng),
        new window.naver.maps.LatLng(maxLat, maxLng)
    );
    // 약간의 여백을 두고 fitBounds
    map.fitBounds(bounds, { margin: 50 }); 
  }
};

watch(() => props.items, () => {
  updateMap();
}, { deep: true });

onMounted(() => {
    // 스크립트가 비동기 로드될 수 있으므로 체크 (index.html에서 로드하므로 보통 바로 사용 가능)
    if (window.naver && window.naver.maps) {
        initMap();
    } else {
        // 혹시 모르니 polling
        const interval = setInterval(() => {
            if (window.naver && window.naver.maps) {
                clearInterval(interval);
                initMap();
            }
        }, 100);
    }
});
</script>
