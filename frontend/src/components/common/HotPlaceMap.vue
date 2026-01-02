<template>
  <div class="relative w-full h-full">
    <div ref="mapContainer" class="w-full h-full rounded-xl bg-gray-800"></div>
    
    <!-- 지도 검색 오버레이 -->
    <div class="absolute top-2 right-2 z-10 flex gap-2 w-80">
      <input 
        v-model="searchKeyword" 
        @keyup.enter="performSearch"
        type="text" 
        placeholder="장소나 주소 검색 (Enter)" 
        class="flex-1 px-3 py-2 rounded-lg bg-white/90 backdrop-blur text-sm border border-gray-300 shadow-md focus:outline-none focus:ring-2 focus:ring-primary text-black"
      />
      <button @click="performSearch" class="px-3 py-2 bg-primary text-white rounded-lg shadow-md hover:bg-blue-600 transition-colors">
        <span class="material-symbols-outlined text-sm">search</span>
      </button>
    </div>

    <!-- 지도 클릭 안내 -->
    <div v-if="!selectedPlace" class="absolute top-16 left-4 right-4 z-10 bg-blue-50 dark:bg-blue-50/90 backdrop-blur-sm rounded-lg shadow-md p-3 border border-blue-200 dark:border-blue-200/60">
      <p class="text-xs text-blue-800 dark:text-blue-800">
        💡 <strong>팁:</strong> 장소명이나 주소를 검색하거나, 지도를 직접 클릭해서 장소를 선택할 수 있습니다.
        <span class="block mt-1 text-blue-600 dark:text-blue-600">
          <span class="inline-block w-2 h-2 bg-green-500 rounded-full mr-1"></span>네이버 검색
          <span class="inline-block w-2 h-2 bg-blue-500 rounded-full mx-2 mr-1"></span>관광정보
        </span>
      </p>
    </div>

    <!-- 검색 결과 목록 (스크롤 가능) -->
    <div v-if="searchResults.length > 0 && !selectedPlace" class="absolute bottom-4 left-4 right-4 z-10 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 max-h-64 overflow-hidden flex flex-col">
      <div class="px-4 py-2 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
        <h3 class="text-sm font-bold text-gray-900 dark:text-white">검색 결과 ({{ searchResults.length }}개)</h3>
        <button 
          @click="clearSearchResults"
          class="text-xs text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200"
        >
          닫기
        </button>
      </div>
      <div class="overflow-y-auto flex-1">
        <div 
          v-for="(item, index) in searchResults" 
          :key="index"
          @click="selectPlaceFromResult(item)"
          class="px-4 py-3 border-b border-gray-100 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
        >
          <div class="flex items-start gap-3">
            <div class="flex-1 min-w-0">
              <h4 class="text-sm font-semibold text-gray-900 dark:text-white truncate">{{ item.title }}</h4>
              <p class="text-xs text-gray-600 dark:text-gray-300 mt-1 line-clamp-1">{{ item.addr1 || item.address || '주소 없음' }}</p>
            </div>
            <div class="flex-shrink-0">
              <span 
                class="inline-block w-2 h-2 rounded-full"
                :class="item.source === 'naver' ? 'bg-green-500' : 'bg-blue-500'"
                :title="item.source === 'naver' ? '네이버 검색' : '관광정보'"
              ></span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 선택된 장소 정보 표시 -->
    <div v-if="selectedPlace" class="absolute bottom-4 left-4 right-4 z-10 bg-white dark:bg-gray-800 rounded-lg shadow-lg p-4 border border-gray-200 dark:border-gray-700">
      <div class="flex items-start justify-between gap-4">
        <div class="flex-1">
          <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-1">{{ selectedPlace.title }}</h3>
          <p class="text-sm text-gray-600 dark:text-gray-300 mb-2">{{ selectedPlace.addr1 || '주소 없음' }}</p>
          <p class="text-xs text-gray-500 dark:text-gray-400">위도: {{ selectedPlace.latitude }}, 경도: {{ selectedPlace.longitude }}</p>
        </div>
        <button 
          @click="clearSelection"
          class="px-3 py-1 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors text-sm"
        >
          취소
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import planApi from '@/services/api/plan';

const emit = defineEmits(['place-selected']);

const mapContainer = ref(null);
const searchKeyword = ref('');
const selectedPlace = ref(null);
const searchResults = ref([]); // 검색 결과 목록
let map = null;
let searchMarkers = [];
let searchInfowindow = null;
let selectedMarker = null;
let clickMarker = null; // 지도 클릭으로 생성된 마커
let geocoder = null;
let alertShown = false; // alert 중복 방지

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
  
  searchInfowindow = new window.naver.maps.InfoWindow({
    disableAnchor: false,
    borderWidth: 0,
    backgroundColor: "transparent",
    pixelOffset: new window.naver.maps.Point(0, -10)
  });

  // Geocoder 초기화는 실제 사용 시점에 수행 (나중에 performNaverGeocodeSearch에서)
  // 여기서는 초기화하지 않고, 사용 시점에 체크하도록 함

  // 지도 클릭 이벤트 - 좌표로 장소 선택
  window.naver.maps.Event.addListener(map, 'click', (e) => {
    const lat = e.coord.lat();
    const lng = e.coord.lng();
    
    // 역지오코딩으로 주소 가져오기 시도
    try {
      if (window.naver && window.naver.maps && window.naver.maps.Service && typeof window.naver.maps.Service.Geocoder === 'function') {
        const geocoder = new window.naver.maps.Service.Geocoder();
        geocoder.reverseGeocode({
          coords: new window.naver.maps.LatLng(lat, lng)
        }, (status, response) => {
          let address = '';
          if (status === window.naver.maps.Service.Status.OK && response.v2) {
            const result = response.v2;
            address = result.address?.roadAddress || result.address?.jibunAddress || '';
          }
          
          // 장소 선택
          const placeTitle = prompt('이 위치의 장소 이름을 입력해주세요:', '');
          if (placeTitle) {
            selectPlaceFromCoordinate(lat, lng, placeTitle, address);
          }
        });
      } else {
        // 역지오코딩 실패 시 좌표만으로 선택
        const placeTitle = prompt('이 위치의 장소 이름을 입력해주세요:', '');
        if (placeTitle) {
          selectPlaceFromCoordinate(lat, lng, placeTitle, '');
        }
      }
    } catch (error) {
      // Geocoder 사용 불가 시 좌표만으로 선택
      const placeTitle = prompt('이 위치의 장소 이름을 입력해주세요:', '');
      if (placeTitle) {
        selectPlaceFromCoordinate(lat, lng, placeTitle, '');
      }
    }
  });
};

const performSearch = async () => {
  if (!searchKeyword.value.trim()) return;
  
  // alert 초기화
  alertShown = false;
  
  // 기존 검색 마커 제거
  searchMarkers.forEach(m => m.setMap(null));
  searchMarkers = [];
  if (searchInfowindow) searchInfowindow.close();
  if (selectedMarker) {
    selectedMarker.setMap(null);
    selectedMarker = null;
  }
  
  // 한국관광공사 API로 여러 타입 동시 검색 (주 검색 방법)
  await performUnifiedTourApiSearch();
  
  // 네이버 지도 Geocoder 검색 (주소 검색 보조, 비동기)
  // 주의: addressSearch는 주소 검색용이므로 장소명 검색에는 제한적
  performNaverGeocodeSearch();
};

// 네이버 지도 Geocoder 검색 (주소/장소명 검색)
const performNaverGeocodeSearch = () => {
  // Geocoder 초기화 시도 (매번 체크하여 안정성 확보)
  try {
    if (!window.naver || !window.naver.maps || !window.naver.maps.Service) {
      console.log('ℹ️ Naver Maps API not fully loaded yet');
      return;
    }
    
    // Geocoder 서비스가 사용 가능한지 확인
    if (typeof window.naver.maps.Service.Geocoder === 'function') {
      // Geocoder가 없으면 새로 생성
      if (!geocoder) {
        geocoder = new window.naver.maps.Service.Geocoder();
      }
    } else {
      console.log('ℹ️ Naver Geocoder service not available (may require additional configuration)');
      return;
    }
  } catch (error) {
    console.warn('⚠️ Failed to initialize Geocoder:', error);
    return;
  }
  
  const keyword = searchKeyword.value.trim();
  console.log(`🔍 Starting Naver Geocoder search for: "${keyword}"`);
  
  geocoder.addressSearch(keyword, (status, response) => {
    if (status === window.naver.maps.Service.Status.ERROR) {
      console.warn('⚠️ Geocoder search error:', status);
      return; // 에러 시 무시 (다른 검색 계속 진행)
    }
    
    if (!response || !response.result || response.result.items.length === 0) {
      console.log(`ℹ️ Geocoder: No results for "${keyword}"`);
      return;
    }
    
    console.log(`✅ Geocoder found ${response.result.items.length} results for "${keyword}"`);
    const bounds = new window.naver.maps.LatLngBounds();
    const items = response.result.items.slice(0, 10); // 최대 10개
    
    items.forEach(item => {
      const lat = parseFloat(item.point.y);
      const lng = parseFloat(item.point.x);
      if (isNaN(lat) || isNaN(lng)) return;
      
      const position = new window.naver.maps.LatLng(lat, lng);
      bounds.extend(position);
      
      const marker = new window.naver.maps.Marker({
        position: position,
        map: map,
        title: item.address || item.title,
        icon: {
          content: `
            <div style="
              width: 12px; height: 12px;
              background-color: #10b981;
              border: 2px solid white;
              border-radius: 50%;
              box-shadow: 0 2px 4px rgba(0,0,0,0.3);
            "></div>
          `,
          anchor: new window.naver.maps.Point(6, 6)
        }
      });
      
      // 검색 결과 목록에 추가 (네이버 Geocoder)
      searchResults.value.push({
        title: item.title || item.address || searchKeyword.value.trim(),
        addr1: item.address || '',
        addr2: '',
        address: item.address || '',
        latitude: lat,
        longitude: lng,
        contentId: null,
        contentTypeId: null,
        firstImage: null,
        firstImage2: null,
        source: 'naver'
      });
      
      // 마커 클릭 시 선택
      window.naver.maps.Event.addListener(marker, 'click', () => {
        selectPlace({
          title: item.title || item.address || searchKeyword.value.trim(),
          addr1: item.address || '',
          addr2: '',
          latitude: lat,
          longitude: lng,
          contentId: null,
          contentTypeId: null,
          firstImage: null,
          firstImage2: null
        });
        
        // 선택된 마커 강조
        if (selectedMarker) {
          selectedMarker.setMap(null);
        }
        
        selectedMarker = new window.naver.maps.Marker({
          position: position,
          map: map,
          icon: {
            content: `
              <div style="
                width: 20px; height: 20px;
                background-color: #ef4444;
                border: 3px solid white;
                border-radius: 50%;
                box-shadow: 0 2px 4px rgba(0,0,0,0.3);
              "></div>
            `,
            anchor: new window.naver.maps.Point(10, 10)
          }
        });
        
        map.setCenter(position);
        map.setZoom(16);
        if (searchInfowindow) searchInfowindow.close();
      });
      
      searchMarkers.push(marker);
    });
    
    if (items.length > 0) {
      console.log(`✅ Naver Geocoder: Added ${items.length} markers to map`);
      map.fitBounds(bounds, { margin: 50 });
      // 네이버 검색에서 결과가 나왔으면 alert 플래그 설정
      alertShown = true;
    }
  });
};

// 타입 이름 반환 함수
const getTypeName = (typeId) => {
  const names = { '12': '관광지', '39': '맛집', '32': '숙소' };
  return names[typeId] || typeId;
};

// 한국관광공사 API 통합 검색 (여러 타입 동시)
const performUnifiedTourApiSearch = async () => {
  try {
    console.log('🔍 Starting Korean Tourism API search for:', searchKeyword.value.trim());
    
    // 관광지, 맛집, 숙소를 모두 검색
    const contentTypeIds = ['12', '39', '32']; // 관광지, 맛집, 숙소
    const searchPromises = contentTypeIds.map(async (typeId) => {
      try {
        const result = await planApi.searchTour({ 
          keyword: searchKeyword.value.trim(), 
          contentTypeId: typeId,
          pageNo: 1, 
          numOfRows: 10 
        });
        console.log(`Search result for type ${typeId}:`, result);
        return result;
      } catch (err) {
        console.error(`Search failed for type ${typeId}:`, err);
        // 에러가 발생해도 null을 반환 (나중에 필터링)
        return null;
      }
    });
    
    const results = await Promise.all(searchPromises);
    console.log('All search results:', results);
    
    // 모든 결과를 통합
    const allItems = [];
    results.forEach((result, index) => {
      if (!result) {
        console.log(`Type ${contentTypeIds[index]}: No result (error or null)`);
        return; // null이면 스킵
      }
      
      let items = [];
      // 백엔드 API는 ApiResponse.success(data) 형태로 반환
      if (result && result.success && result.data) {
        items = Array.isArray(result.data) ? result.data : [];
        console.log(`✅ Type ${contentTypeIds[index]} (${getTypeName(contentTypeIds[index])}): Found ${items.length} items`);
        if (items.length > 0) {
          console.log(`Sample item from type ${contentTypeIds[index]}:`, JSON.stringify(items[0], null, 2));
        }
      } else {
        console.warn(`⚠️ Type ${contentTypeIds[index]}: Unexpected result format`, result);
        if (result) {
          console.warn(`Result structure:`, {
            hasSuccess: 'success' in result,
            hasData: 'data' in result,
            success: result.success,
            message: result.message,
            dataType: result.data ? typeof result.data : 'null'
          });
        }
      }
      allItems.push(...items);
    });
    
    console.log(`📊 Total items before deduplication: ${allItems.length}`);
    
    if (allItems.length === 0) {
      console.warn('⚠️ No items found from Korean Tourism API at all!');
      console.log('This might mean:');
      console.log('1. API returned no results for the keyword');
      console.log('2. API response format is different than expected');
      console.log('3. API key or service is having issues');
    }
    
    // 중복 제거 (제목과 좌표 기준)
    const uniqueItems = [];
    const seen = new Set();
    let skippedCount = 0;
    
    allItems.forEach((item, index) => {
      const title = item.title || '';
      
      // 백엔드는 소문자 mapx, mapy로 전송 (TravelPlanService.java 참고)
      // 한국관광공사 API는 경도(mapx), 위도(mapy)를 반환
      // 주의: 한국관광공사 API는 GRS80/TM 좌표계를 사용할 수 있으나,
      // 일반적으로 경도/위도로 제공되므로 그대로 사용
      const mapxStr = String(item.mapx || item.mapX || item.map_x || '').trim();
      const mapyStr = String(item.mapy || item.mapY || item.map_y || '').trim();
      
      // 문자열 좌표를 숫자로 변환
      const lng = mapxStr ? parseFloat(mapxStr) : NaN;  // 경도 (x)
      const lat = mapyStr ? parseFloat(mapyStr) : NaN;  // 위도 (y)
      
      // 좌표 유효성 검사 (한국 위도 범위: 33-43, 경도 범위: 124-132)
      const isValidLat = !isNaN(lat) && lat >= 33 && lat <= 43;
      const isValidLng = !isNaN(lng) && lng >= 124 && lng <= 132;
      
      if (title && isValidLat && isValidLng) {
        const key = `${title}-${lat.toFixed(6)}-${lng.toFixed(6)}`;
        if (!seen.has(key)) {
          seen.add(key);
          uniqueItems.push(item);
        }
      } else {
        skippedCount++;
        // 처음 몇 개만 상세 로그 (너무 많이 출력 방지)
        if (skippedCount <= 5) {
          console.warn(`⚠️ Skipping item "${title}" (${index + 1}/${allItems.length}):`, {
            reason: !title ? 'no title' : (!isValidLat || !isValidLng) ? 'invalid coordinates' : 'unknown',
            mapx: mapxStr || '(empty)',
            mapy: mapyStr || '(empty)',
            parsedLat: lat,
            parsedLng: lng,
            isValidLat,
            isValidLng
          });
        }
      }
    });
    
    if (skippedCount > 0) {
      console.warn(`⚠️ Skipped ${skippedCount} items due to invalid data`);
    }
    
    console.log(`✅ Korean Tourism API search: Found ${uniqueItems.length} unique valid items after deduplication`);
    
    // 네이버 Geocoder 검색은 비동기이므로 약간의 지연을 두고 최종 결과 확인
    // alert 중복 방지를 위해 플래그 사용
    setTimeout(() => {
      // 한국관광공사 API 결과가 없고, 네이버 Geocoder 결과도 없을 때만 알림
      if (!alertShown && uniqueItems.length === 0 && searchMarkers.length === 0) {
        alertShown = true;
        console.error('❌ No search results found from both Korean Tourism API and Naver Geocoder');
        console.log('💡 Tip: Try searching with more specific keywords or addresses');
        alert('검색 결과가 없습니다.\n\n한국관광공사 API에 등록된 장소만 검색됩니다.\n주소나 다른 키워드로 검색하거나, 지도를 직접 클릭해서 장소를 선택할 수 있습니다.');
      } else if (uniqueItems.length === 0 && searchMarkers.length > 0) {
        console.log(`✅ Found ${searchMarkers.length} results from Naver Geocoder (Korean Tourism API had no results)`);
      }
    }, 2000); // 네이버 검색 완료를 기다림 (더 여유있게)

    const bounds = new window.naver.maps.LatLngBounds();

    uniqueItems.forEach(item => {
      // 백엔드는 소문자 mapx, mapy로 보냄
      // 한국관광공사 API는 경도(mapx), 위도(mapy)를 반환
      const mapxStr = item.mapx || item.mapX || item.map_x || '';
      const mapyStr = item.mapy || item.mapY || item.map_y || '';
      
      // 문자열 좌표를 숫자로 변환
      const lng = mapxStr ? parseFloat(mapxStr) : 0;  // 경도 (x)
      const lat = mapyStr ? parseFloat(mapyStr) : 0;  // 위도 (y)
      
      if (!lat || !lng || isNaN(lat) || isNaN(lng) || lat === 0 || lng === 0) {
        console.warn(`Cannot create marker for "${item.title}": invalid coordinates`, {
          mapx: mapxStr,
          mapy: mapyStr,
          parsedLat: lat,
          parsedLng: lng
        });
        return;
      }

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
              background-color: #3b82f6;
              border: 2px solid white;
              border-radius: 50%;
              box-shadow: 0 2px 4px rgba(0,0,0,0.3);
            "></div>
          `,
          anchor: new window.naver.maps.Point(6, 6)
        }
      });
      
      // 마커 클릭 시 선택
      window.naver.maps.Event.addListener(marker, 'click', () => {
        selectPlace({
          title: item.title,
          addr1: item.addr1 || item.addr || '',
          addr2: item.addr2 || '',
          latitude: lat,
          longitude: lng,
          contentId: item.contentid || item.contentId,
          contentTypeId: item.contenttypeid || item.contentTypeId,
          firstImage: item.firstimage || item.firstImage || item.image,
          firstImage2: item.firstimage2 || item.firstImage2
        });
        
        // 선택된 마커 강조
        if (selectedMarker) {
          selectedMarker.setMap(null);
        }
        
        selectedMarker = new window.naver.maps.Marker({
          position: position,
          map: map,
          icon: {
            content: `
              <div style="
                width: 20px; height: 20px;
                background-color: #ef4444;
                border: 3px solid white;
                border-radius: 50%;
                box-shadow: 0 2px 4px rgba(0,0,0,0.3);
              "></div>
            `,
            anchor: new window.naver.maps.Point(10, 10)
          }
        });
        
        map.setCenter(position);
        map.setZoom(16);
        searchInfowindow.close();
      });

      searchMarkers.push(marker);
      
      // 검색 결과 목록에 추가 (source 표시용)
      searchResults.value.push({
        ...item,
        source: 'kto',
        latitude: lat,
        longitude: lng
      });
    });

    // 네이버 검색 결과와 한국관광공사 결과가 모두 있으면 bounds 조정
    if (searchMarkers.length > 0) {
      // 이미 네이버 검색에서 bounds가 설정되었을 수 있으므로 모든 마커를 포함하도록 확장
      searchMarkers.forEach(marker => {
        bounds.extend(marker.getPosition());
      });
      map.fitBounds(bounds, { margin: 50 });
      // 마커가 있으면 alert 표시 안 함
      alertShown = true;
    }

  } catch (error) {
    console.error('Search failed:', error);
    // 에러 메시지를 더 자세히 표시
    const errorMessage = error.response?.data?.message || error.message || '알 수 없는 오류';
    console.error('Error details:', error);
    alert(`장소 검색 중 오류가 발생했습니다: ${errorMessage}\n\n콘솔을 확인해주세요.`);
  }
};

const selectPlace = (place) => {
  selectedPlace.value = place;
  emit('place-selected', place);
  
  // 클릭 마커 제거
  if (clickMarker) {
    clickMarker.setMap(null);
    clickMarker = null;
  }
};

// 좌표로부터 장소 선택 (지도 클릭 시)
const selectPlaceFromCoordinate = (lat, lng, title, address) => {
  // 기존 선택 마커 제거
  if (selectedMarker) {
    selectedMarker.setMap(null);
  }
  if (clickMarker) {
    clickMarker.setMap(null);
  }
  
  const position = new window.naver.maps.LatLng(lat, lng);
  
  // 선택된 마커 표시
  selectedMarker = new window.naver.maps.Marker({
    position: position,
    map: map,
    icon: {
      content: `
        <div style="
          width: 20px; height: 20px;
          background-color: #ef4444;
          border: 3px solid white;
          border-radius: 50%;
          box-shadow: 0 2px 4px rgba(0,0,0,0.3);
        "></div>
      `,
      anchor: new window.naver.maps.Point(10, 10)
    }
  });
  
  selectPlace({
    title: title,
    addr1: address || '',
    addr2: '',
    latitude: lat,
    longitude: lng,
    contentId: null,
    contentTypeId: null,
    firstImage: null,
    firstImage2: null
  });
  
  map.setCenter(position);
  map.setZoom(16);
};

const clearSelection = () => {
  selectedPlace.value = null;
  if (selectedMarker) {
    selectedMarker.setMap(null);
    selectedMarker = null;
  }
  if (clickMarker) {
    clickMarker.setMap(null);
    clickMarker = null;
  }
  emit('place-selected', null);
};

// 검색 결과 목록에서 장소 선택
const selectPlaceFromResult = (item) => {
  selectPlace({
    title: item.title,
    addr1: item.addr1 || item.address || '',
    addr2: item.addr2 || '',
    latitude: item.latitude,
    longitude: item.longitude,
    contentId: item.contentId || item.contentid || null,
    contentTypeId: item.contentTypeId || item.contenttypeid || null,
    firstImage: item.firstImage || item.firstimage || null,
    firstImage2: item.firstImage2 || item.firstimage2 || null
  });
  
  // 선택된 마커 강조
  if (selectedMarker) {
    selectedMarker.setMap(null);
  }
  
  const position = new window.naver.maps.LatLng(item.latitude, item.longitude);
  selectedMarker = new window.naver.maps.Marker({
    position: position,
    map: map,
    icon: {
      content: `
        <div style="
          width: 20px; height: 20px;
          background-color: #ef4444;
          border: 3px solid white;
          border-radius: 50%;
          box-shadow: 0 2px 4px rgba(0,0,0,0.3);
        "></div>
      `,
      anchor: new window.naver.maps.Point(10, 10)
    }
  });
  
  map.setCenter(position);
  map.setZoom(16);
  
  // 검색 결과 목록 닫기 (selectedPlace가 설정되면 자동으로 닫힘)
};

// 검색 결과 목록 닫기
const clearSearchResults = () => {
  searchResults.value = [];
};

onMounted(() => {
  if (window.naver && window.naver.maps) {
    initMap();
  } else {
    const interval = setInterval(() => {
      if (window.naver && window.naver.maps) {
        clearInterval(interval);
        initMap();
      }
    }, 100);
  }
});
</script>

