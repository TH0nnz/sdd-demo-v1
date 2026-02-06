import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ErrorBoundary from '@/components/common/ErrorBoundary.vue'

describe('ErrorBoundary.vue', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(ErrorBoundary, {
      slots: {
        default: '<div>Test Content</div>',
      },
    })
  })

  it('應該渲染子內容 - 無錯誤', () => {
    expect(wrapper.html()).toContain('Test Content')
    expect(wrapper.find('.error-boundary').exists()).toBe(false)
  })

  it('應該隱藏錯誤狀態初始化', () => {
    expect(wrapper.vm.hasError).toBe(false)
  })

  it('應該處理錯誤並顯示錯誤邊界', async () => {
    const error = new Error('Test error message')
    wrapper.vm.handleError(error, 'TestComponent')

    await wrapper.vm.$nextTick()

    expect(wrapper.vm.hasError).toBe(true)
    expect(wrapper.vm.errorMessage).toBe('Test error message')
    expect(wrapper.html()).toContain('發生錯誤')
  })

  it('應該重置錯誤狀態', async () => {
    const error = new Error('Test error')
    wrapper.vm.handleError(error, 'TestComponent')

    await wrapper.vm.$nextTick()
    expect(wrapper.vm.hasError).toBe(true)

    wrapper.vm.resetError()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.hasError).toBe(false)
  })

  it('應該切換詳細資訊顯示', async () => {
    const error = new Error('Test error')
    wrapper.vm.handleError(error, 'TestComponent')

    await wrapper.vm.$nextTick()

    expect(wrapper.vm.showDetails).toBe(false)
    wrapper.vm.toggleDetails()
    expect(wrapper.vm.showDetails).toBe(true)
  })
})
