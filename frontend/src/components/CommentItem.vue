<template>
  <div class="flex gap-4">
    <div
      v-if="comment.authorProfileImage"
      class="size-8 flex-shrink-0 rounded-full bg-cover bg-center bg-no-repeat"
      :style="{ backgroundImage: `url('${comment.authorProfileImage}')` }"
    ></div>
    <div
      v-else
      class="size-8 flex-shrink-0 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center"
    >
      <span class="material-symbols-outlined text-xs text-slate-500">person</span>
    </div>
    <div class="flex-grow">
      <div class="flex items-baseline gap-2 justify-between">
        <div class="flex items-baseline gap-2">
          <span class="font-semibold text-gray-900 dark:text-white text-sm">{{ comment.authorNickname || comment.authorName || '작성자' }}</span>
          <span class="text-xs text-gray-600 dark:text-slate-500">{{ formatDate(comment.createdAt) }}</span>
        </div>
        <div v-if="isMyComment" class="flex items-center gap-2">
          <button
            @click="startEdit"
            class="text-xs text-gray-600 dark:text-slate-400 hover:text-primary transition-colors flex items-center gap-1"
          >
            <span class="material-symbols-outlined text-sm">edit</span>
            <span>수정</span>
          </button>
          <button
            @click="handleDelete"
            class="text-xs text-gray-600 dark:text-slate-400 hover:text-red-400 transition-colors flex items-center gap-1"
          >
            <span class="material-symbols-outlined text-sm">delete</span>
            <span>삭제</span>
          </button>
        </div>
      </div>
      
      <div v-if="!editMode" class="mt-1">
        <p class="text-gray-700 dark:text-slate-300 whitespace-pre-wrap text-sm">{{ comment.content }}</p>
      </div>
      <div v-else class="mt-1">
            <textarea
              v-model="editContent"
              class="w-full resize-none rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark text-gray-900 dark:text-slate-300 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
              rows="3"
            ></textarea>
            <div class="mt-2 flex justify-end gap-2">
              <button
                @click="cancelEdit"
                class="rounded-lg px-3 py-1.5 text-sm font-medium text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white transition-colors"
              >
                취소
              </button>
          <button
            @click="saveComment"
            class="rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-white transition hover:bg-blue-500"
          >
            저장
          </button>
        </div>
      </div>
      
      <div v-if="!isReply" class="mt-2 flex items-center gap-4">
        <button
          @click="showReplyForm = !showReplyForm"
          class="text-xs text-gray-600 dark:text-slate-400 hover:text-primary transition-colors flex items-center gap-1"
        >
          <span class="material-symbols-outlined text-sm">reply</span>
          <span>답글</span>
        </button>
      </div>
      
      <!-- 답글 작성 폼 -->
      <div v-if="!isReply && showReplyForm" class="mt-3 ml-0">
        <div class="flex items-start gap-3">
          <div
            v-if="authStore.user?.profileImage"
            class="size-7 flex-shrink-0 rounded-full bg-cover bg-center bg-no-repeat"
            :style="{ backgroundImage: `url('${authStore.user.profileImage}')` }"
          ></div>
          <div
            v-else
            class="size-7 flex-shrink-0 rounded-full bg-slate-300 dark:bg-card-dark flex items-center justify-center"
          >
            <span class="material-symbols-outlined text-xs text-slate-500">person</span>
          </div>
          <div class="flex-grow">
            <textarea
              v-model="replyContent"
              class="w-full resize-none rounded-lg border border-gray-200 dark:border-border-dark bg-gray-50 dark:bg-background-dark text-gray-900 dark:text-slate-300 text-sm px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary"
              placeholder="답글을 입력하세요..."
              rows="2"
            ></textarea>
            <div class="mt-2 flex justify-end gap-2">
              <button
                @click="cancelReply"
                class="rounded-lg px-3 py-1.5 text-xs font-medium text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white transition-colors"
              >
                취소
              </button>
              <button
                @click="addReply"
                class="rounded-lg bg-primary px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-blue-500"
              >
                작성
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 대댓글 목록 -->
      <div v-if="!isReply && comment.replies && comment.replies.length > 0" class="mt-2">
        <div
          v-for="reply in comment.replies"
          :key="reply.commentId"
          class="ml-8 mt-4 border-l-2 border-gray-300 dark:border-slate-700 pl-4"
        >
          <CommentItem
            :comment="reply"
            :current-user-id="currentUserId"
            :post-id="postId"
            :is-reply="true"
            @reload="$emit('reload')"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import boardApi from '@/services/api/board'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  currentUserId: {
    type: Number,
    default: null
  },
  postId: {
    type: Number,
    required: true
  },
  isReply: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['reload'])

const authStore = useAuthStore()
const editMode = ref(false)
const editContent = ref('')
const showReplyForm = ref(false)
const replyContent = ref('')

const isMyComment = computed(() => {
  return props.currentUserId && props.comment.userId === props.currentUserId
})

const startEdit = () => {
  editMode.value = true
  editContent.value = props.comment.content
}

const cancelEdit = () => {
  editMode.value = false
  editContent.value = ''
}

const saveComment = async () => {
  if (!editContent.value.trim()) {
    alert('댓글 내용을 입력해주세요')
    return
  }
  
  try {
    const result = await boardApi.updateComment(props.comment.commentId, {
      content: editContent.value
    })
    
    if (result.success) {
      editMode.value = false
      emit('reload')
    } else {
      alert(result.message || '댓글 수정에 실패했습니다')
    }
  } catch (error) {
    console.error('댓글 수정 오류:', error)
    alert('댓글 수정 중 오류가 발생했습니다')
  }
}

const handleDelete = async () => {
  if (!confirm('정말 이 댓글을 삭제하시겠습니까?')) return
  
  try {
    const result = await boardApi.deleteComment(props.comment.commentId)
    if (result.success) {
      alert('댓글이 삭제되었습니다')
      emit('reload')
    } else {
      alert(result.message || '댓글 삭제에 실패했습니다')
    }
  } catch (error) {
    console.error('댓글 삭제 오류:', error)
    alert('댓글 삭제 중 오류가 발생했습니다')
  }
}

const cancelReply = () => {
  showReplyForm.value = false
  replyContent.value = ''
}

const addReply = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다')
    return
  }
  
  if (!replyContent.value.trim()) {
    alert('답글을 입력해주세요')
    return
  }
  
  try {
    const result = await boardApi.addComment(props.postId, {
      content: replyContent.value,
      parentCommentId: props.comment.commentId
    })
    
    if (result.success) {
      showReplyForm.value = false
      replyContent.value = ''
      emit('reload')
    } else {
      alert(result.message || '답글 작성에 실패했습니다')
    }
  } catch (error) {
    console.error('답글 작성 오류:', error)
    alert('답글 작성 중 오류가 발생했습니다')
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

