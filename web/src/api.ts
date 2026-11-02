import type { Account, ChatMessage, Connection, InstagramShare, Profile, VerificationCase } from './types'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers)
  if (!(init?.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  const response = await fetch(path, {
    ...init,
    credentials: 'include',
    headers,
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null) as {
      detail?: string
      message?: string
      error?: string
      title?: string
    } | null
    throw new Error(body?.detail ?? body?.message ?? body?.error ?? body?.title ?? `Request failed (${response.status})`)
  }

  const body = await response.text()
  return (body ? JSON.parse(body) : undefined) as T
}

export const getCurrentProfile = () => request<Account>('/api/profiles/me')
export const getDiscoverProfiles = (search: string) =>
  request<Profile[]>(`/api/profiles/discover?search=${encodeURIComponent(search)}`)
export const getConnections = () => request<Connection[]>('/api/connections')
export const requestConnection = (profileId: number) =>
  request<Connection>(`/api/connections/${profileId}`, { method: 'POST' })
export const acceptConnection = (requestId: number) =>
  request<Connection>(`/api/connections/${requestId}/accept`, { method: 'PATCH' })
export const sendPhoneCode = (
  phoneNumber: string,
  email: string,
  password: string,
  name: string,
  gender: string,
  headline: string,
  city: string,
  story: string,
  interests: string,
  instagramHandle: string,
) =>
  request<{ message: string; demoCode: string }>('/api/auth/send-code', {
    method: 'POST',
    body: JSON.stringify({ phoneNumber, email, password, name, gender, headline, city, story, interests, instagramHandle }),
  })
export const signIn = (identifier: string, password: string) =>
  request<Account>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ identifier, password }),
  })
export const verifyPhoneCode = (phoneNumber: string, code: string) =>
  request<Account>('/api/auth/verify', {
    method: 'POST',
    body: JSON.stringify({ phoneNumber, code }),
  })
export const logout = () => request<void>('/api/auth/logout', { method: 'POST' })
export const getMessages = (requestId: number) =>
  request<ChatMessage[]>(`/api/connections/${requestId}/messages`)
export const sendMessage = (requestId: number, body: string) =>
  request<ChatMessage>(`/api/connections/${requestId}/messages`, {
    method: 'POST',
    body: JSON.stringify({ body }),
  })
export const getInstagramShares = (requestId: number) =>
  request<InstagramShare[]>(`/api/connections/${requestId}/instagram-shares`)
export const shareInstagram = (requestId: number) =>
  request<InstagramShare[]>(`/api/connections/${requestId}/instagram-share`, {
    method: 'POST',
  })
export const hideInstagram = (requestId: number) =>
  request<InstagramShare[]>(`/api/connections/${requestId}/instagram-share`, { method: 'DELETE' })
export const getVerificationQueue = () => request<VerificationCase[]>('/api/admin/verification-queue')
export const reviewVerification = (profileId: number, approved: boolean) =>
  request<void>(`/api/admin/profiles/${profileId}/verification`, {
    method: 'PATCH',
    body: JSON.stringify({ approved }),
  })
export const uploadVoiceRecording = (recording: File) => {
  const data = new FormData()
  data.append('recording', recording)
  return request<void>('/api/profiles/me/voice-verification', { method: 'POST', body: data })
}
export const getVoiceRecordingUrl = async (profileId: number) => {
  const response = await fetch(`/api/admin/profiles/${profileId}/voice`, { credentials: 'include' })
  if (!response.ok) throw new Error(`Could not load voice recording (${response.status})`)
  return URL.createObjectURL(await response.blob())
}
