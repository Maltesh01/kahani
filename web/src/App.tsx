import { useEffect, useMemo, useRef, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import {
  ArrowDownUp,
  ArrowRight,
  ArrowLeft,
  Bell,
  Check,
  Compass,
  Heart,
  Instagram,
  MapPin,
  MessageCircle,
  Search,
  Sparkles,
  Users,
} from 'lucide-react'
import {
  acceptConnection,
  getConnections,
  getCurrentProfile,
  getDiscoverProfiles,
  logout,
  requestConnection,
  getMessages,
  getInstagramShares,
  hideInstagram,
  sendMessage,
  shareInstagram,
  sendPhoneCode,
  verifyPhoneCode,
  uploadVoiceRecording,
  getVerificationQueue,
  reviewVerification,
  getVoiceRecordingUrl,
  signIn,
} from './api'
import type { Account, ChatMessage, Connection, InstagramShare, Profile, VerificationCase } from './types'

type View = 'discover' | 'connections' | 'chat' | 'admin'

function Avatar({ profile, size = 'medium' }: { profile: Profile; size?: 'small' | 'medium' | 'large' }) {
  return (
    <div className={`avatar avatar-${size}`} aria-hidden="true">
      {profile.name.split(' ').map((part) => part[0]).slice(0, 2).join('')}
    </div>
  )
}

function AuthScreen({ onAuthenticated }: { onAuthenticated: (account: Account) => void }) {
  const [loginMode, setLoginMode] = useState(false)
  const [phoneNumber, setPhoneNumber] = useState('')
  const [email, setEmail] = useState('')
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [name, setName] = useState('')
  const [gender, setGender] = useState<'MAN' | 'WOMAN'>('MAN')
  const [headline, setHeadline] = useState('')
  const [city, setCity] = useState('')
  const [story, setStory] = useState('')
  const [interests, setInterests] = useState('')
  const [instagramHandle, setInstagramHandle] = useState('')
  const [code, setCode] = useState('')
  const [challengeSent, setChallengeSent] = useState(false)
  const [demoCode, setDemoCode] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function handleSendCode(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    setBusy(true)
    setError('')
    try {
      const response = await sendPhoneCode(phoneNumber, email, password, name, gender, headline, city, story, interests, instagramHandle)
      setDemoCode(response.demoCode)
      setCode(response.demoCode)
      setChallengeSent(true)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not send a verification code.')
    } finally {
      setBusy(false)
    }
  }

  async function handleSignIn(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      onAuthenticated(await signIn(identifier, password))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not sign in.')
    } finally {
      setBusy(false)
    }
  }

  async function handleVerify(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      onAuthenticated(await verifyPhoneCode(phoneNumber, code))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not verify this phone number.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <a className="brand auth-brand" href="#" onClick={(event) => event.preventDefault()}>
          <img className="brand-mark" src="/kahani-mark.svg" alt="" />
          <span className="brand-wordmark">Kahani<span className="brand-period">.</span><small>STORIES, WITHOUT THE SWIPE</small></span>
        </a>
        <div className="eyebrow"><span className="eyebrow-line" /> A LITTLE MORE YOU, A LITTLE MORE US</div>
        {!challengeSent && (
          <div className="auth-tabs" role="tablist" aria-label="Account access">
            <button type="button" role="tab" aria-selected={!loginMode} className={!loginMode ? 'auth-tab active' : 'auth-tab'} onClick={() => { setLoginMode(false); setError('') }}>Sign up</button>
            <button type="button" role="tab" aria-selected={loginMode} className={loginMode ? 'auth-tab active' : 'auth-tab'} onClick={() => { setLoginMode(true); setError('') }}>Sign in</button>
          </div>
        )}
        <h1>{challengeSent ? 'Verify your number.' : loginMode ? 'Welcome back.' : 'Be known by your story.'}</h1>
        <p className="page-description">Kahani brings people together through what they have lived, learned, and care about—not profile photos.</p>
        <form className="auth-form" onSubmit={challengeSent ? handleVerify : loginMode ? handleSignIn : handleSendCode}>
          {!challengeSent && loginMode && (
            <>
              <label>Phone number or email<input value={identifier} onChange={(event) => setIdentifier(event.target.value)} autoComplete="username" required placeholder="+919876543210 or you@example.com" /></label>
              <label>Password<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete="current-password" required /></label>
            </>
          )}
          {!challengeSent && !loginMode && (
            <>
              <div className="auth-policy">Your Kahani account ID will be generated after phone verification. Sign in later with your phone number or email and password.</div>
              <label>Your name<input value={name} onChange={(event) => setName(event.target.value)} minLength={2} maxLength={80} required placeholder="What should we call you?" /></label>
              <label>I identify as
                <select value={gender} onChange={(event) => setGender(event.target.value as 'MAN' | 'WOMAN')}>
                  <option value="MAN">Man</option>
                  <option value="WOMAN">Woman</option>
                </select>
              </label>
              <label>Your story<textarea value={story} onChange={(event) => setStory(event.target.value)} minLength={40} maxLength={1200} required placeholder="Share a moment, a lesson, or something you care about. No photos needed." rows={5} /></label>
              <label>Story title<input value={headline} onChange={(event) => setHeadline(event.target.value)} maxLength={100} placeholder="A short title (optional)" /></label>
              <label>Interests<input value={interests} onChange={(event) => setInterests(event.target.value)} maxLength={300} placeholder="Design, books, running..." /></label>
              <label>City<input value={city} onChange={(event) => setCity(event.target.value)} maxLength={100} placeholder="Optional" /></label>
              <label>Instagram username<input value={instagramHandle} onChange={(event) => setInstagramHandle(event.target.value)} maxLength={31} required placeholder="@yourhandle — kept private" /></label>
              <div className="auth-policy">
                Your Instagram is required for admin verification, but stays private. Optional voice verification is separate and is only shared with Kahani admins.
              </div>
              <label>Email address<input value={email} onChange={(event) => setEmail(event.target.value)} type="email" autoComplete="email" required placeholder="you@example.com" /></label>
              <label>Create password<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete="new-password" minLength={8} maxLength={72} required /></label>
              <label>Confirm password<input value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} type="password" autoComplete="new-password" minLength={8} maxLength={72} required /></label>
            </>
          )}
          {!challengeSent && !loginMode && <label>Phone number<input value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} type="tel" autoComplete="tel" required placeholder="+919876543210" /></label>}
          {challengeSent && <label>Phone number<input value={phoneNumber} type="tel" autoComplete="tel" readOnly required /></label>}
          {challengeSent && (
            <>
              <label>Verification code<input value={code} onChange={(event) => setCode(event.target.value)} inputMode="numeric" autoComplete="one-time-code" required /></label>
              <div className="demo-code-note">Local demo code: <strong>{demoCode}</strong></div>
            </>
          )}
          {error && <div className="error-banner" role="alert">{error}</div>}
          <button className="auth-submit" type="submit" disabled={busy}>{busy ? 'Please wait...' : challengeSent ? 'Verify and create account' : loginMode ? 'Sign in' : 'Create account'}</button>
          {challengeSent && <button className="auth-back" type="button" onClick={() => { setChallengeSent(false); setError('') }}>Back to sign-up details</button>}
        </form>
        {import.meta.env.DEV && (
          <div className="auth-demo">
            Demo sign-in: phone <strong>+15550000001</strong> or email <strong>alexmorgandemo@kahani.demo</strong>; password <strong>KahaniDemo123!</strong>. Admin email <strong>kahaniadmindemo@kahani.demo</strong>, same password. Signup OTP: <strong>123456</strong>.
          </div>
        )}
        <div className="auth-footer"><Heart size={13} /> Your story comes first. Profile photos are never requested or shown.</div>
      </section>
    </main>
  )
}

function PendingVerificationView({ account, onVoiceUploaded }: {
  account: Account
  onVoiceUploaded: () => void
}) {
  return (
    <div className="pending-page">
      <div className="eyebrow"><span className="eyebrow-line" /> YOUR STORY IS WITH OUR TEAM</div>
      <h1>We’re getting to know you<span className="title-dot">.</span></h1>
      <p className="page-description">Your Kahani profile is private while an admin reviews your Instagram and story. Once approved, you can discover people and connect.</p>
      <div className="review-status"><span className="review-status-dot" /> Verification status: <strong>{account.verificationStatus.toLowerCase()}</strong></div>
      <section className="verification-self">
        <div className="verification-self-heading">
          <div className="empty-icon"><MessageCircle size={20} /></div>
          <div><h2>Optional voice verification</h2><p>Your recording is private, reviewed only by admins, and deleted when they make a decision.</p></div>
        </div>
        <VoiceUpload submitted={account.voiceRecordingSubmitted} onUploaded={onVoiceUploaded} />
      </section>
    </div>
  )
}

function VoiceUpload({ submitted, onUploaded }: { submitted: boolean; onUploaded: () => void }) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file) return
    setError('')
    if (!file.type.startsWith('audio/')) {
      setError('Choose an audio recording.')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      setError('Voice recordings must be 5 MB or smaller.')
      return
    }
    setBusy(true)
    try {
      await uploadVoiceRecording(file)
      onUploaded()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not upload this recording.')
    } finally {
      setBusy(false)
      event.target.value = ''
    }
  }

  return (
    <div className="voice-upload">
      <input ref={inputRef} type="file" accept="audio/*" hidden onChange={(event) => void handleFileChange(event)} />
      <span>{submitted ? 'A voice recording is ready for admin review.' : 'You can optionally add a short voice recording to help the admin team review your account.'}</span>
      <button className="share-button" onClick={() => inputRef.current?.click()} disabled={busy}>
        {busy ? 'Uploading...' : submitted ? 'Replace recording' : 'Add optional voice recording'}
      </button>
      {error && <div className="error-banner" role="alert">{error}</div>}
    </div>
  )
}

function AdminPanel() {
  const [queue, setQueue] = useState<VerificationCase[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)

  const refresh = () => {
    setLoading(true)
    getVerificationQueue()
      .then(setQueue)
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : 'Could not load verification queue.'))
      .finally(() => setLoading(false))
  }

  useEffect(refresh, [])

  async function decide(profileId: number, approved: boolean) {
    setBusyId(profileId)
    setError('')
    try {
      await reviewVerification(profileId, approved)
      setQueue((current) => current.filter((entry) => entry.profileId !== profileId))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not update verification.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <>
      <div className="connections-intro">
        <div className="eyebrow"><span className="eyebrow-line" /> KAHANI COMMUNITY CARE</div>
        <h1>Verification queue<span className="title-dot">.</span></h1>
        <p className="page-description">Review the story and Instagram profile. Voice is optional. Do not treat voice as proof of gender or identity.</p>
      </div>
      {error && <div className="error-banner" role="alert">{error}</div>}
      {loading ? <div className="empty-state"><div className="loader" /> Loading reviews...</div> : queue.length === 0 ? (
        <div className="empty-state"><div className="empty-icon"><Check size={20} /></div><strong>All caught up</strong><span>There are no accounts waiting for review.</span></div>
      ) : (
        <div className="review-queue">
          {queue.map((entry) => (
            <VerificationCard
              key={entry.profileId}
              entry={entry}
              busy={busyId === entry.profileId}
              onDecision={(approved) => void decide(entry.profileId, approved)}
            />
          ))}
        </div>
      )}
    </>
  )
}

function VerificationCard({
  entry,
  busy,
  onDecision,
}: {
  entry: VerificationCase
  busy: boolean
  onDecision: (approved: boolean) => void
}) {
  const [voiceUrl, setVoiceUrl] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (!entry.voiceRecordingSubmitted) return
    let active = true
    let objectUrl = ''
    getVoiceRecordingUrl(entry.profileId)
      .then((url) => {
        objectUrl = url
        if (active) setVoiceUrl(url)
        else URL.revokeObjectURL(url)
      })
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : 'Could not load voice recording.'))
    return () => {
      active = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [entry.profileId, entry.voiceRecordingSubmitted])

  return (
    <article className="verification-card">
      <div className="verification-card-top"><div><strong>{entry.name}</strong><span>{entry.gender.toLowerCase()} · {entry.city}</span></div><span className="review-status-dot" /> Pending</div>
      <h3>{entry.headline}</h3>
      <p className="verification-story">{entry.story}</p>
      <a className="verification-instagram" href={`https://www.instagram.com/${encodeURIComponent(entry.instagramHandle)}/`} target="_blank" rel="noreferrer">
        <Instagram size={15} /> @{entry.instagramHandle} <span>Review Instagram</span>
      </a>
      <div className="verification-voice">
        {entry.voiceRecordingSubmitted ? voiceUrl ? <audio controls src={voiceUrl}>Audio playback is not supported.</audio> : <span>Loading optional voice sample…</span> : <span>No voice recording submitted. It is optional.</span>}
      </div>
      {error && <div className="error-banner" role="alert">{error}</div>}
      <div className="verification-actions">
        <button className="reject-button" onClick={() => onDecision(false)} disabled={busy}>Reject</button>
        <button className="connect-button" onClick={() => onDecision(true)} disabled={busy}>{busy ? 'Saving...' : 'Approve account'}</button>
      </div>
    </article>
  )
}

function ChatView({
  connection,
  currentProfileId,
  currentInstagramHandle,
  onBack,
}: {
  connection: Connection
  currentProfileId: number
  currentInstagramHandle: string
  onBack: () => void
}) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [shares, setShares] = useState<InstagramShare[]>([])
  const [body, setBody] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let active = true
    Promise.all([getMessages(connection.requestId), getInstagramShares(connection.requestId)])
      .then(([conversation, instagramShares]) => {
        if (!active) return
        setMessages(conversation)
        setShares(instagramShares)
      })
      .catch((cause: unknown) => {
        if (active) setError(cause instanceof Error ? cause.message : 'Could not load this conversation.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [connection.requestId])

  async function handleSend(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const message = await sendMessage(connection.requestId, body)
      setMessages((current) => [...current, message])
      setBody('')
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not send this message.')
    } finally {
      setBusy(false)
    }
  }

  async function handleShare(show: boolean) {
    setBusy(true)
    setError('')
    try {
      setShares(show
        ? await shareInstagram(connection.requestId)
        : await hideInstagram(connection.requestId))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not share Instagram.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="chat-page">
      <button className="back-link" onClick={onBack}><ArrowLeft size={15} /> All connections</button>
      <header className="chat-header">
        <Avatar profile={connection.profile} />
        <div className="connection-copy">
          <h1>{connection.profile.name}</h1>
          <p>{connection.profile.headline}</p>
        </div>
        <span className="connected-status"><Check size={14} /> Matched</span>
      </header>
      <div className="chat-safety-note"><Heart size={14} /> You’re in control. Instagram links stay hidden unless each person chooses to reveal theirs.</div>
      <section className="instagram-panel">
        <div><Instagram size={17} /><strong>Instagram</strong><span>Only visible after you choose to share</span></div>
        {currentInstagramHandle && (
          <button className="share-button share-toggle" onClick={() => void handleShare(!shares.some((share) => share.senderId === currentProfileId))} disabled={busy}>
            {shares.some((share) => share.senderId === currentProfileId) ? 'Hide my Instagram' : 'Share my Instagram'}
          </button>
        )}
        {shares.length > 0 && <div className="shared-handles">
          {shares.map((share) => (
            <a key={share.senderId} href={`https://www.instagram.com/${encodeURIComponent(share.handle)}/`} target="_blank" rel="noreferrer">
              {share.senderId === currentProfileId ? 'You' : connection.profile.name}: @{share.handle}
            </a>
          ))}
        </div>}
      </section>
      {error && <div className="error-banner" role="alert">{error}</div>}
      <section className="chat-panel">
        <div className="chat-messages">
          {loading ? <div className="empty-state"><div className="loader" /> Loading conversation...</div> : messages.length === 0 ? (
            <div className="empty-state chat-empty"><div className="empty-icon"><MessageCircle size={20} /></div><strong>Start with a simple hello</strong><span>Good conversations begin one message at a time.</span></div>
          ) : messages.map((message) => (
            <div className={message.senderId === currentProfileId ? 'message-row mine' : 'message-row'} key={message.id}>
              <p>{message.body}</p>
              <time>{new Date(message.createdAt).toLocaleString()}</time>
            </div>
          ))}
        </div>
        <form className="chat-compose" onSubmit={handleSend}>
          <input value={body} onChange={(event) => setBody(event.target.value)} maxLength={2000} placeholder="Write a thoughtful hello..." aria-label="Message" />
          <button className="connect-button" type="submit" disabled={busy || !body.trim()}><MessageCircle size={15} /> Send</button>
        </form>
      </section>
    </div>
  )
}

function App() {
  const [view, setView] = useState<View>('discover')
  const [account, setAccount] = useState<Account | null>(null)
  const [activeChat, setActiveChat] = useState<Connection | null>(null)
  const [profiles, setProfiles] = useState<Profile[]>([])
  const [connections, setConnections] = useState<Connection[]>([])
  const [search, setSearch] = useState('')
  const [sortByName, setSortByName] = useState(false)
  const [loading, setLoading] = useState(true)
  const [authChecked, setAuthChecked] = useState(false)
  const [busyIds, setBusyIds] = useState<number[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    getCurrentProfile()
      .then(async (currentAccount) => {
        if (!active) return
        setAccount(currentAccount)
        const currentConnections = await getConnections()
        if (!active) return
        setConnections(currentConnections)
      })
      .catch((cause: unknown) => {
        if (active && !(cause instanceof Error && (cause.message.includes('401') || cause.message.includes('verify your phone number')))) {
          setError(cause instanceof Error ? cause.message : 'Could not load Kahani.')
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
          setAuthChecked(true)
        }
      })
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    if (!account) return
    let active = true
    getDiscoverProfiles(search)
      .then((results) => {
        if (active) setProfiles(results)
      })
      .catch((cause: unknown) => {
        if (active) setError(cause instanceof Error ? cause.message : 'Could not load people.')
      })
    return () => {
      active = false
    }
  }, [search, account])

  const incomingCount = useMemo(
    () => connections.filter((connection) => connection.direction === 'INCOMING').length,
    [connections],
  )
  const connectedCount = useMemo(
    () => connections.filter((connection) => connection.direction === 'CONNECTED').length,
    [connections],
  )
  const connectionByProfile = useMemo(
    () => new Map(connections.map((connection) => [connection.profile.id, connection])),
    [connections],
  )
  const sortedProfiles = useMemo(
    () => sortByName
      ? [...profiles].sort((first, second) => first.name.localeCompare(second.name))
      : profiles,
    [profiles, sortByName],
  )

  async function handleConnect(profileId: number) {
    setBusyIds((ids) => [...ids, profileId])
    setError('')
    try {
      const connection = await requestConnection(profileId)
      const currentAccount = await getCurrentProfile()
      setAccount(currentAccount)
      setConnections((current) => [
        connection,
        ...current.filter((item) => item.profile.id !== profileId),
      ])
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not send your request.')
    } finally {
      setBusyIds((ids) => ids.filter((id) => id !== profileId))
    }
  }

  async function handleAccept(connection: Connection) {
    setBusyIds((ids) => [...ids, connection.profile.id])
    setError('')
    try {
      const accepted = await acceptConnection(connection.requestId)
      setConnections((current) =>
        current.map((item) => item.requestId === accepted.requestId ? accepted : item),
      )
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not accept this request.')
    } finally {
      setBusyIds((ids) => ids.filter((id) => id !== connection.profile.id))
    }
  }

  async function handleLogout() {
    await logout()
    setAccount(null)
    setConnections([])
    setView('discover')
    setActiveChat(null)
  }

  if (!authChecked) {
    return <div className="auth-loading"><div className="loader" /> Loading Kahani...</div>
  }
  if (!account) {
    return <AuthScreen onAuthenticated={(authenticated) => {
      setAccount(authenticated)
      setError('')
    }} />
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#" onClick={(event) => event.preventDefault()}>
          <img className="brand-mark" src="/kahani-mark.svg" alt="" />
          <span className="brand-wordmark">Kahani<span className="brand-period">.</span><small>STORIES, WITHOUT THE SWIPE</small></span>
        </a>

        <div className="sidebar-label">YOUR SPACE</div>
        <nav className="primary-nav" aria-label="Main navigation">
          <button className={view === 'discover' ? 'nav-item active' : 'nav-item'} onClick={() => setView('discover')}>
            <Compass size={18} />
            <span>Discover</span>
          </button>
          <button className={view === 'connections' ? 'nav-item active' : 'nav-item'} onClick={() => setView('connections')}>
            <Users size={18} />
            <span>Connections</span>
            {incomingCount > 0 && <span className="nav-count">{incomingCount}</span>}
          </button>
          <button className={view === 'chat' ? 'nav-item active' : 'nav-item'} onClick={() => setView('connections')}>
            <MessageCircle size={18} />
            <span>Messages</span>
            <span className="nav-count">{connectedCount}</span>
          </button>
          {account.admin && (
            <button className={view === 'admin' ? 'nav-item active' : 'nav-item'} onClick={() => setView('admin')}>
              <Check size={18} />
              <span>Admin review</span>
            </button>
          )}
        </nav>

        <div className="sidebar-note">
          <div className="note-icon"><Heart size={15} /></div>
          <p>Stories make room for a different kind of connection.</p>
          <span>Come as you are. No photos.</span>
        </div>

        {account && (
          <div className="account-card">
            <Avatar profile={account.profile} size="small" />
            <span className="account-copy">
              <strong>{account.profile.name}</strong>
              <small>{account.accountId}</small>
            </span>
            <button className="logout-button" onClick={() => void handleLogout()}>Log out</button>
          </div>
        )}
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb"><span>KAHANI</span><span className="breadcrumb-separator">/</span><strong>{view === 'admin' ? 'ADMIN REVIEW' : view === 'discover' ? 'STORIES' : view === 'chat' ? 'MESSAGES' : 'CONNECTIONS'}</strong></div>
          <div className="topbar-right">
            <span className="online-indicator"><i /> Your corner of the world</span>
            {account.gender === 'MAN' && <span className="credit-badge"><Sparkles size={13} /> {account.creditsBalance} credits</span>}
            <button className="icon-button notification-button" aria-label={`${incomingCount} new connection requests`} onClick={() => setView('connections')}>
              <Bell size={18} />
              {incomingCount > 0 && <i />}
            </button>
          </div>
        </header>

        <section className="page-wrap">
          {view === 'admin' && account.admin ? (
            <AdminPanel />
          ) : view === 'discover' && account.accountStatus === 'PENDING_VERIFICATION' ? (
            <PendingVerificationView
              account={account}
              onVoiceUploaded={() => {
                getCurrentProfile().then(setAccount).catch((cause: unknown) =>
                  setError(cause instanceof Error ? cause.message : 'Could not refresh account status.'))
              }}
            />
          ) : view === 'discover' ? (
            <>
              <div className="welcome-row">
                <div>
                  <div className="eyebrow"><span className="eyebrow-line" /> MEET THE PERSON BEHIND THE STORY</div>
                  <h1>Stories bring us<br className="mobile-break" /> closer<span className="title-dot">.</span></h1>
                  <p className="page-description">Discover people through what they care about and the stories they carry.<br className="desktop-break" /> No profile photos. Just people, in their own words.</p>
                </div>
                <div className="welcome-art" aria-hidden="true">
                  <span className="art-ring ring-one" />
                  <span className="art-ring ring-two" />
                  <span className="art-spark"><Sparkles size={17} /></span>
                  <span className="art-quote">“</span>
                </div>
              </div>

              <div className="toolbar">
                <label className="search-box">
                  <Search size={17} />
                  <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by name, interest, or place..." />
                  {search && <button className="clear-search" onClick={() => setSearch('')} aria-label="Clear search">×</button>}
                </label>
                <button className="sort-button" onClick={() => setSortByName((current) => !current)}>
                  <ArrowDownUp size={15} /><span>{sortByName ? 'Name' : 'Best match'}</span>
                </button>
              </div>

              {error && <div className="error-banner" role="alert">{error}</div>}
              {account.gender === 'MAN' && (!account.subscriptionActive || account.creditsBalance < 1) && (
                <div className="notice-banner" role="status">
                  {account.subscriptionActive
                    ? 'You’re out of credits. Razorpay checkout is not configured yet; subscription price and credit allowance are still to be decided.'
                    : 'An active subscription and 1 credit per connection are required. Razorpay checkout is not configured yet.'}
                </div>
              )}
              <div className="section-heading">
                <div><h2>{search ? 'Stories that match' : 'Stories to discover'}</h2><p>{sortedProfiles.length} stor{sortedProfiles.length === 1 ? 'y' : 'ies'} to meet</p></div>
                <div className="match-legend"><span className="legend-dot" /> Shared interests</div>
              </div>

              {loading ? (
                <div className="empty-state"><div className="loader" /><span>Finding your people...</span></div>
              ) : sortedProfiles.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon"><Search size={21} /></div>
                  <strong>No stories found just yet</strong>
                  <span>Try another name, interest, or city.</span>
                </div>
              ) : (
                <div className="people-grid">
                  {sortedProfiles.map((person, index) => {
                    const connection = connectionByProfile.get(person.id)
                    const isBusy = busyIds.includes(person.id)
                    return (
                      <article className="person-card" key={person.id} style={{ animationDelay: `${index * 55}ms` }}>
                        <div className="card-top">
                          <div className="match-pill"><Sparkles size={12} /> {person.matchPercent}% match</div>
                          <span className="story-mark" aria-hidden="true">“</span>
                        </div>
                        <div className="person-identity">
                          <Avatar profile={person} size="large" />
                          <div className="person-heading">
                            <h3>{person.name}</h3>
                            <p>{person.headline}</p>
                            <span className="person-location"><MapPin size={12} />{person.city}</span>
                          </div>
                        </div>
                        <div className="story-excerpt"><span className="field-label">A STORY FROM {person.name.toUpperCase()}</span><p>{person.story}</p></div>
                        <div className="interests-block">
                          <span className="field-label">THINGS IN COMMON</span>
                          <div className="interest-list">
                            {person.interests.slice(0, 4).map((interest) => (
                              <span className={person.sharedInterests.includes(interest.toLowerCase()) || person.sharedInterests.includes(interest) ? 'interest-tag shared' : 'interest-tag'} key={interest}>
                                {interest}
                              </span>
                            ))}
                            {person.interests.length > 4 && <span className="interest-overflow">+{person.interests.length - 4}</span>}
                          </div>
                        </div>
                        <div className="card-footer">
                          <div className="shared-caption">
                            {person.sharedInterests.length > 0
                              ? <><span className="shared-avatar"><Heart size={11} fill="currentColor" /></span>{person.sharedInterests.slice(0, 2).join(' · ')}</>
                              : 'A new perspective awaits'}
                          </div>
                          {connection?.direction === 'CONNECTED' ? (
                            <button className="connect-button" onClick={() => { setActiveChat(connection); setView('chat') }}><MessageCircle size={14} /> Message</button>
                          ) : connection?.direction === 'OUTGOING' ? (
                            <button className="connect-button pending-button" disabled><Check size={15} /> Requested</button>
                          ) : connection?.direction === 'INCOMING' ? (
                            <button className="connect-button" onClick={() => handleAccept(connection)} disabled={isBusy}>
                              {isBusy ? 'Accepting...' : 'Accept'} <ArrowRight size={14} />
                            </button>
                          ) : (
                            <button className="connect-button" onClick={() => handleConnect(person.id)} disabled={isBusy}>
                              {isBusy ? 'Sending...' : 'Say hello'} <ArrowRight size={14} />
                            </button>
                          )}
                        </div>
                      </article>
                    )
                  })}
                </div>
              )}
              <div className="bottom-note"><Sparkles size={14} /> A good connection can start with one small hello.</div>
            </>
          ) : view === 'connections' ? (
            <ConnectionsView
              connections={connections}
              busyIds={busyIds}
              error={error}
              incomingCount={incomingCount}
              connectedCount={connectedCount}
              onAccept={handleAccept}
              onMessage={(connection) => { setActiveChat(connection); setView('chat') }}
            />
          ) : activeChat ? (
            <ChatView
              connection={activeChat}
              currentProfileId={account.profile.id}
              currentInstagramHandle={account.instagramHandle}
              onBack={() => setView('connections')}
            />
          ) : (
            <div className="empty-state"><strong>Choose a connection to start chatting</strong><button className="connect-button" onClick={() => setView('connections')}>View connections</button></div>
          )}
        </section>
      </main>
    </div>
  )
}

function ConnectionsView({
  connections,
  busyIds,
  error,
  incomingCount,
  connectedCount,
  onAccept,
  onMessage,
}: {
  connections: Connection[]
  busyIds: number[]
  error: string
  incomingCount: number
  connectedCount: number
  onAccept: (connection: Connection) => void
  onMessage: (connection: Connection) => void
}) {
  const [filter, setFilter] = useState<'all' | 'requests' | 'connected'>('all')
  const visible = connections.filter((connection) => {
    if (filter === 'requests') return connection.direction === 'INCOMING'
    if (filter === 'connected') return connection.direction === 'CONNECTED'
    return true
  })

  return (
    <>
      <div className="connections-intro">
        <div className="eyebrow"><span className="eyebrow-line" /> YOUR PEOPLE, YOUR PACE</div>
        <h1>Connections<span className="title-dot">.</span></h1>
        <p className="page-description">Every good friendship starts somewhere. See who's reaching out.</p>
      </div>
      <div className="connection-stats">
        <div><span className="stat-number">{connectedCount}</span><span>connections</span></div>
        <span className="stat-divider" />
        <div><span className="stat-number">{incomingCount}</span><span>waiting to meet you</span></div>
      </div>
      <div className="connection-tabs">
        <button className={filter === 'all' ? 'connection-tab selected' : 'connection-tab'} onClick={() => setFilter('all')}>All <span>{connections.length}</span></button>
        <button className={filter === 'requests' ? 'connection-tab selected' : 'connection-tab'} onClick={() => setFilter('requests')}>Requests <span>{incomingCount}</span></button>
        <button className={filter === 'connected' ? 'connection-tab selected' : 'connection-tab'} onClick={() => setFilter('connected')}>Connected <span>{connectedCount}</span></button>
      </div>
      {error && <div className="error-banner" role="alert">{error}</div>}
      {visible.length === 0 ? (
        <div className="empty-state connection-empty">
          <div className="empty-icon"><Users size={21} /></div>
          <strong>{filter === 'requests' ? 'No new requests' : 'Your next connection is out there'}</strong>
          <span>{filter === 'requests' ? 'When someone reaches out, you’ll find them here.' : 'Explore people who share the things you love.'}</span>
        </div>
      ) : (
        <div className="connection-list">
          {visible.map((connection) => (
            <article className="connection-row" key={connection.requestId}>
              <Avatar profile={connection.profile} size="medium" />
              <div className="connection-copy">
                <div className="connection-name">{connection.profile.name}<span>{connection.direction === 'INCOMING' ? 'wants to connect' : connection.direction === 'CONNECTED' ? 'you’re connected' : 'request sent'}</span></div>
                <p>{connection.profile.headline}</p>
                <div className="connection-meta"><MapPin size={12} /> {connection.profile.city}<span className="meta-dot">·</span>{connection.profile.interests.slice(0, 3).join(' · ')}</div>
              </div>
              {connection.direction === 'INCOMING' ? (
                <button className="connect-button" onClick={() => onAccept(connection)} disabled={busyIds.includes(connection.profile.id)}>
                  {busyIds.includes(connection.profile.id) ? 'Accepting...' : 'Accept'} <ArrowRight size={14} />
                </button>
              ) : connection.direction === 'CONNECTED' ? (
                <button className="connect-button" onClick={() => onMessage(connection)}><MessageCircle size={14} /> Message</button>
              ) : (
                <span className="requested-status">Request sent</span>
              )}
            </article>
          ))}
        </div>
      )}
    </>
  )
}

export default App
