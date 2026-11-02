import { useEffect, useMemo, useRef, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import {
  ArrowRight,
  ArrowLeft,
  Bell,
  Check,
  Compass,
  Heart,
  Instagram,
  LogOut,
  MapPin,
  MessageCircle,
  MoreHorizontal,
  Sparkles,
  Star,
  Users,
  Wallet,
} from 'lucide-react'
import {
  acceptConnection,
  getConnections,
  getCreditPurchaseQueue,
  getCurrentProfile,
  getDiscoverProfiles,
  getMyCreditPurchases,
  logout,
  requestConnection,
  getMessages,
  getInstagramShares,
  hideInstagram,
  sendMessage,
  shareInstagram,
  sendEmailCode,
  sendPhoneCode,
  startSignup,
  verifyEmailCode,
  verifyPhoneCode,
  uploadVoiceRecording,
  getVerificationQueue,
  reviewVerification,
  reviewCreditPurchase,
  requestCreditPurchase,
  getVoiceRecordingUrl,
  signIn,
  sendAccountIdRecoveryCode,
  recoverAccountId,
} from './api'
import type { Account, ChatMessage, Connection, CreditPurchase, InstagramShare, Profile, VerificationCase } from './types'

type View = 'discover' | 'connections' | 'messages' | 'chat' | 'more' | 'admin'
type MoreSection = 'menu' | 'matches' | 'favourites' | 'skipped' | 'membership'

const purposeOptions = [
  ['SILLY_CONVERSATION', 'Silly conversation'],
  ['DEEP_CONVERSATION', 'Deep conversation'],
  ['HANGOUT_PARTNER', 'Hangout partner'],
  ['FRIENDSHIP', 'Friendship'],
  ['LOVE', 'Love'],
  ['ALL_OF_THE_ABOVE', 'All of the above'],
] as const

const languageOptions = [
  'Assamese', 'Bengali', 'Bodo', 'Dogri', 'Gujarati', 'Hindi', 'Kannada',
  'Kashmiri', 'Konkani', 'Maithili', 'Malayalam', 'Manipuri', 'Marathi',
  'Nepali', 'Odia', 'Punjabi', 'Sanskrit', 'Santali', 'Sindhi', 'Tamil',
  'Telugu', 'Urdu',
]

const interestOptions = [
  ['ARTS', 'Arts'], ['BOOKS_READING', 'Books and reading'], ['BOARD_GAMES', 'Board games'],
  ['COOKING', 'Cooking'], ['CULTURE', 'Culture'], ['DANCE', 'Dance'], ['DESIGN', 'Design'],
  ['ENVIRONMENT', 'Environment'], ['FASHION', 'Fashion'], ['FILM_TV', 'Film and TV'],
  ['FITNESS', 'Fitness'], ['FOOD', 'Food'], ['GAMING', 'Gaming'], ['GARDENING', 'Gardening'],
  ['HISTORY', 'History'], ['MINDFULNESS', 'Mindfulness'], ['MUSIC', 'Music'],
  ['OUTDOORS', 'Outdoors and hiking'], ['PETS', 'Pets'], ['PHOTOGRAPHY', 'Photography'],
  ['PODCASTS', 'Podcasts'], ['SCIENCE', 'Science'], ['SPIRITUALITY', 'Spirituality'],
  ['SPORTS', 'Sports'], ['TECHNOLOGY', 'Technology'], ['TRAVEL', 'Travel'],
  ['VOLUNTEERING', 'Volunteering'], ['WRITING', 'Writing'], ['COMEDY', 'Comedy'],
  ['ENTREPRENEURSHIP', 'Entrepreneurship'], ['PERSONAL_GROWTH', 'Personal growth'],
] as const

function Avatar({ profile, size = 'medium' }: { profile: Profile; size?: 'small' | 'medium' | 'large' }) {
  return (
    <div className={`avatar avatar-${size}`} aria-hidden="true">
      {profile.name.split(' ').map((part) => part[0]).slice(0, 2).join('')}
    </div>
  )
}

function AuthScreen({ onAuthenticated }: { onAuthenticated: (account: Account) => void }) {
  const [loginMode, setLoginMode] = useState(false)
  const [recoveryMode, setRecoveryMode] = useState(false)
  const [recoveryEmail, setRecoveryEmail] = useState('')
  const [recoveryCode, setRecoveryCode] = useState('')
  const [recoveryCodeSent, setRecoveryCodeSent] = useState(false)
  const [recoveryDemoCode, setRecoveryDemoCode] = useState('')
  const [recoveryResult, setRecoveryResult] = useState<{ message: string; accountId: string | null } | null>(null)
  const [signupStep, setSignupStep] = useState(1)
  const [phoneNumber, setPhoneNumber] = useState('')
  const [email, setEmail] = useState('')
  const [emailCode, setEmailCode] = useState('')
  const [emailDemoCode, setEmailDemoCode] = useState('')
  const [emailCodeSent, setEmailCodeSent] = useState(false)
  const [emailVerificationToken, setEmailVerificationToken] = useState('')
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [name, setName] = useState('')
  const [gender, setGender] = useState<'MAN' | 'WOMAN'>('MAN')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [relationshipStatus, setRelationshipStatus] = useState('')
  const [city, setCity] = useState('')
  const [purposes, setPurposes] = useState<string[]>([])
  const [signupToken, setSignupToken] = useState('')
  const [accountId, setAccountId] = useState('')
  const [occupation, setOccupation] = useState('')
  const [nativeCity, setNativeCity] = useState('')
  const [languages, setLanguages] = useState<string[]>([])
  const [story, setStory] = useState('')
  const [interests, setInterests] = useState<string[]>([])
  const [interestNotes, setInterestNotes] = useState<Record<string, string>>({})
  const [valuesNote, setValuesNote] = useState('')
  const [instagramUrl, setInstagramUrl] = useState('')
  const [phoneCode, setPhoneCode] = useState('')
  const [phoneDemoCode, setPhoneDemoCode] = useState('')
  const [phoneChallengeSent, setPhoneChallengeSent] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function handleSendEmailCode() {
    setBusy(true)
    setError('')
    try {
      const response = await sendEmailCode(email)
      setEmailDemoCode(response.demoCode)
      setEmailCode(response.demoCode)
      setEmailCodeSent(true)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not send an email verification code.')
    } finally {
      setBusy(false)
    }
  }

  async function handleVerifyEmail() {
    setBusy(true)
    setError('')
    try {
      const response = await verifyEmailCode(email, emailCode)
      setEmailVerificationToken(response.verificationToken)
      setEmailCodeSent(false)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not verify this email address.')
    } finally {
      setBusy(false)
    }
  }

  async function handleStartSignup() {
    if (purposes.length === 0) {
      setError('Choose at least one thing you are here for.')
      return
    }
    setBusy(true)
    setError('')
    try {
      const response = await startSignup({
        emailVerificationToken,
        name,
        phoneNumber,
        dateOfBirth,
        gender,
        relationshipStatus,
        city,
        email,
        purposes,
      })
      setSignupToken(response.signupToken)
      setAccountId(response.accountId)
      setSignupStep(3)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not start your signup.')
    } finally {
      setBusy(false)
    }
  }

  async function handleSendCode() {
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    const wordCount = story.trim().split(/\s+/).filter(Boolean).length
    if (wordCount < 250) {
      setError('Your story needs at least 250 words before you can submit it.')
      return
    }
    setBusy(true)
    setError('')
    try {
      const response = await sendPhoneCode({
        signupToken,
        password,
        occupation,
        nativeCity,
        languages,
        interests,
        interestNotes,
        valuesNote,
        story,
        instagramUrl,
      })
      setPhoneDemoCode(response.demoCode)
      setPhoneCode(response.demoCode)
      setPhoneChallengeSent(true)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not send a verification code.')
    } finally {
      setBusy(false)
    }
  }

  async function handleSignIn() {
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

  async function handleSendAccountIdRecoveryCode() {
    setBusy(true)
    setError('')
    try {
      const response = await sendAccountIdRecoveryCode(recoveryEmail)
      setRecoveryDemoCode(response.demoCode)
      setRecoveryCode(response.demoCode)
      setRecoveryCodeSent(true)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not send an account recovery code.')
    } finally {
      setBusy(false)
    }
  }

  async function handleRecoverAccountId() {
    setBusy(true)
    setError('')
    try {
      setRecoveryResult(await recoverAccountId(recoveryEmail, recoveryCode))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not recover your Kahani ID.')
    } finally {
      setBusy(false)
    }
  }

  async function handleVerify() {
    setBusy(true)
    setError('')
    try {
      onAuthenticated(await verifyPhoneCode(phoneNumber, phoneCode))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not verify this phone number.')
    } finally {
      setBusy(false)
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (loginMode) {
      if (!recoveryMode) {
        void handleSignIn()
      } else if (recoveryResult) {
        setRecoveryMode(false)
        setRecoveryResult(null)
        setRecoveryCodeSent(false)
        setRecoveryCode('')
        setRecoveryDemoCode('')
      } else if (recoveryCodeSent) {
        void handleRecoverAccountId()
      } else {
        void handleSendAccountIdRecoveryCode()
      }
    } else if (phoneChallengeSent) {
      void handleVerify()
    } else if (signupStep === 1) {
      if (emailVerificationToken) {
        setSignupStep(2)
        setError('')
      } else {
        void handleSendEmailCode()
      }
    } else if (signupStep === 2) {
      void handleStartSignup()
    } else if (signupStep === 3) {
      if (languages.length === 0) {
        setError('Choose at least one language you speak.')
        return
      }
      setSignupStep(4)
      setError('')
    } else if (signupStep === 4) {
      if (interests.length === 0) {
        setError('Choose at least one interest.')
        return
      }
      if (interests.some((interest) => (interestNotes[interest] ?? '').trim().length < 8)) {
        setError('Add a short note of at least 8 characters for every selected interest.')
        return
      }
      setSignupStep(5)
      setError('')
    } else if (signupStep === 5) {
      setSignupStep(6)
      setError('')
    } else if (signupStep === 6) {
      void handleSendCode()
    }
  }

  function toggleSelection(selection: string, selected: string[], update: (next: string[]) => void) {
    update(selected.includes(selection)
      ? selected.filter((item) => item !== selection)
      : [...selected, selection])
  }

  function togglePurpose(selection: string) {
    const allPurposes = purposeOptions.map(([value]) => value)
    if (selection === 'ALL_OF_THE_ABOVE') {
      setPurposes(purposes.includes(selection) ? [] : allPurposes)
      return
    }
    const currentPurposes = purposes.filter((purpose) => purpose !== 'ALL_OF_THE_ABOVE')
    setPurposes(currentPurposes.includes(selection)
      ? currentPurposes.filter((purpose) => purpose !== selection)
      : [...currentPurposes, selection])
  }

  const minimumBirthDate = new Date()
  minimumBirthDate.setFullYear(minimumBirthDate.getFullYear() - 18)
  const maxBirthDate = `${minimumBirthDate.getFullYear()}-${String(minimumBirthDate.getMonth() + 1).padStart(2, '0')}-${String(minimumBirthDate.getDate()).padStart(2, '0')}`

  return (
    <main className="auth-page">
      <section className="auth-card">
        <a className="brand auth-brand" href="#" onClick={(event) => event.preventDefault()}>
          <img className="brand-mark" src="/kahani-mark.svg" alt="" />
          <span className="brand-wordmark">Kahani<span className="brand-period">.</span><small>STORIES, WITHOUT THE SWIPE</small></span>
        </a>
        <div className="eyebrow"><span className="eyebrow-line" /> A LITTLE MORE YOU, A LITTLE MORE US</div>
        {!phoneChallengeSent && (
          <div className="auth-tabs" role="tablist" aria-label="Account access">
            <button type="button" role="tab" aria-selected={!loginMode} className={!loginMode ? 'auth-tab active' : 'auth-tab'} onClick={() => { setLoginMode(false); setError('') }}>Sign up</button>
            <button type="button" role="tab" aria-selected={loginMode} className={loginMode ? 'auth-tab active' : 'auth-tab'} onClick={() => { setLoginMode(true); setError('') }}>Sign in</button>
          </div>
        )}
        <h1>{phoneChallengeSent ? 'Verify your number.' : loginMode ? recoveryMode ? 'Recover your Kahani ID.' : 'Welcome back.' : signupStep === 1 ? 'Start with the basics.' : signupStep === 2 ? 'What are you here for?' : signupStep === 3 ? 'A little more about you.' : signupStep === 4 ? 'Choose your interests.' : signupStep === 5 ? 'What matters most?' : signupStep === 6 ? 'Tell us your story.' : 'Your signup'}</h1>
        <p className="page-description">Kahani brings people together through what they have lived, learned, and care about—not profile photos.</p>
        <form className="auth-form" onSubmit={handleSubmit}>
          {!phoneChallengeSent && loginMode && (
            recoveryMode ? (
              recoveryResult ? (
                <>
                  <div className="auth-policy">{recoveryResult.message}</div>
                  {recoveryResult.accountId && <div className="signup-id-card"><span>Your Kahani ID</span><strong>{recoveryResult.accountId}</strong><small>Use this ID or your email address to sign in.</small></div>}
                </>
              ) : (
                <>
                  <label>Email address on your account<input value={recoveryEmail} onChange={(event) => setRecoveryEmail(event.target.value)} type="email" autoComplete="email" required placeholder="you@example.com" /></label>
                  {recoveryCodeSent && (
                    <>
                      <label>Email verification code<input value={recoveryCode} onChange={(event) => setRecoveryCode(event.target.value)} inputMode="numeric" autoComplete="one-time-code" required /></label>
                      {import.meta.env.DEV && <div className="demo-code-note">Local demo code: <strong>{recoveryDemoCode}</strong></div>}
                    </>
                  )}
                  <div className="auth-policy">We’ll verify your email before showing your Kahani ID.</div>
                </>
              )
            ) : (
              <>
                <label>Email address or Kahani ID<input value={identifier} onChange={(event) => setIdentifier(event.target.value)} autoComplete="username" required placeholder="you@example.com or KANI-XXXXXXXXXX" /></label>
                <label>Password<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete="current-password" required /></label>
              </>
            )
          )}
          {!phoneChallengeSent && !loginMode && signupStep === 1 && (
            <>
              <div className="signup-progress"><span className="active">1. Your details</span><span>2. Purpose</span><span>3–6. Profile</span></div>
              <label>Your name<input value={name} onChange={(event) => setName(event.target.value)} minLength={2} maxLength={80} required placeholder="What should we call you?" /></label>
              <label>WhatsApp number<input value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} type="tel" autoComplete="tel" required placeholder="+919876543210" /></label>
              <label>Date of birth<input value={dateOfBirth} onChange={(event) => setDateOfBirth(event.target.value)} type="date" max={maxBirthDate} required /></label>
              <label>I identify as
                <select value={gender} onChange={(event) => setGender(event.target.value as 'MAN' | 'WOMAN')}>
                  <option value="MAN">Man</option>
                  <option value="WOMAN">Woman</option>
                </select>
              </label>
              <label>Relationship status
                <select value={relationshipStatus} onChange={(event) => setRelationshipStatus(event.target.value)} required>
                  <option value="" disabled>Select your relationship status</option>
                  <option value="SINGLE">Single</option>
                  <option value="DATING_NOT_SURE_YET">Dating, not sure yet</option>
                  <option value="RECENT_BREAKUP">Recent break up</option>
                  <option value="AWAITING_DIVORCE">Awaiting divorce</option>
                  <option value="COMMITTED_RELATIONSHIP">Committed relationship</option>
                  <option value="OPEN_RELATIONSHIP">Open relationship</option>
                </select>
              </label>
              <label>Current city<input value={city} onChange={(event) => setCity(event.target.value)} maxLength={100} required placeholder="Where are you based?" /></label>
              <label>Email address<input value={email} onChange={(event) => {
                setEmail(event.target.value)
                setEmailVerificationToken('')
                setEmailCodeSent(false)
                setEmailCode('')
              }} type="email" autoComplete="email" required placeholder="you@example.com" /></label>
              {emailCodeSent && !emailVerificationToken && (
                <>
                  <label>Email verification code<input value={emailCode} onChange={(event) => setEmailCode(event.target.value)} inputMode="numeric" autoComplete="one-time-code" required /></label>
                  {import.meta.env.DEV && <div className="demo-code-note">Local demo code: <strong>{emailDemoCode}</strong></div>}
                  <button className="auth-secondary" type="button" onClick={() => void handleVerifyEmail()} disabled={busy || !emailCode}>Verify email</button>
                </>
              )}
              {emailVerificationToken && <div className="auth-policy">Email verified. Continue to choose what you are looking for.</div>}
              <div className="auth-policy">You must be 18 or older to create a Kahani account. Verify your email before continuing.</div>
            </>
          )}
          {!phoneChallengeSent && !loginMode && signupStep > 1 && (
            <>
              <div className="signup-progress">
                {['1. Details', '2. Purpose', '3. About you', '4. Interests', '5. Values', '6. Story'].map((step, index) => (
                  <span key={step} className={index + 1 < signupStep ? 'complete' : index + 1 === signupStep ? 'active' : ''}>{step}</span>
                ))}
              </div>
              {signupStep === 2 && (
                <>
                  <div className="auth-policy">Choose one or more. Your Kahani ID will be created when you continue.</div>
                  <div className="signup-choice-list">
                    {purposeOptions.map(([value, label]) => (
                      <label className="signup-choice" key={value}>
                        <input type="checkbox" checked={purposes.includes(value)} onChange={() => togglePurpose(value)} />
                        <span>{label}</span>
                      </label>
                    ))}
                  </div>
                </>
              )}
              {signupStep === 3 && (
                <>
                  <div className="signup-id-card"><span>Your Kahani ID</span><strong>{accountId}</strong><small>Keep this ID safe. It is unique to your account.</small></div>
                  <label>What do you do for a living?<input value={occupation} onChange={(event) => setOccupation(event.target.value)} maxLength={120} required placeholder="Your work, studies, or what keeps you busy" /></label>
                  <label>Native city<input value={nativeCity} onChange={(event) => setNativeCity(event.target.value)} maxLength={100} required placeholder="Your hometown" /></label>
                  <div className="signup-field-label">Languages you speak <span>Select all that apply</span></div>
                  <div className="signup-language-list">
                    {languageOptions.map((language) => {
                      const key = language.toUpperCase()
                      return <label className="signup-choice" key={key}><input type="checkbox" checked={languages.includes(key)} onChange={() => toggleSelection(key, languages, setLanguages)} /><span>{language}</span></label>
                    })}
                  </div>
                </>
              )}
              {signupStep === 4 && (
                <>
                  <div className="auth-policy">Choose your interests, then add a short note about each one.</div>
                  <div className="signup-choice-list">
                    {interestOptions.map(([value, label]) => (
                      <label className="signup-choice" key={value}>
                        <input type="checkbox" checked={interests.includes(value)} onChange={() => toggleSelection(value, interests, setInterests)} />
                        <span>{label}</span>
                      </label>
                    ))}
                  </div>
                  {interests.map((interest) => (
                    <label key={interest}>{interestOptions.find(([value]) => value === interest)?.[1]} — tell us a little more
                      <textarea value={interestNotes[interest] ?? ''} onChange={(event) => setInterestNotes({ ...interestNotes, [interest]: event.target.value })} minLength={8} maxLength={300} required placeholder="What do you enjoy about it?" rows={3} />
                    </label>
                  ))}
                </>
              )}
              {signupStep === 5 && (
                <label>What do you value the most?
                  <textarea value={valuesNote} onChange={(event) => setValuesNote(event.target.value)} minLength={20} maxLength={1000} required placeholder="Tell us what matters most to you and why." rows={5} />
                </label>
              )}
              {signupStep === 6 && (
                <>
                  <label>Your story<textarea value={story} onChange={(event) => setStory(event.target.value)} maxLength={20000} required placeholder="Express yourself. Your story must be at least 250 words." rows={9} /></label>
                  <div className={`story-word-count ${story.trim().split(/\s+/).filter(Boolean).length >= 250 ? 'complete' : ''}`}>
                    {story.trim().split(/\s+/).filter(Boolean).length} / 250 words minimum
                  </div>
                  <label>Instagram profile link<input value={instagramUrl} onChange={(event) => setInstagramUrl(event.target.value)} type="url" required placeholder="https://www.instagram.com/yourhandle/" /></label>
                  <div className="auth-policy">Your Instagram link is private and used by the Kahani team for verification only.</div>
                  <label>Create password<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete="new-password" minLength={8} maxLength={72} required /></label>
                  <label>Confirm password<input value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} type="password" autoComplete="new-password" minLength={8} maxLength={72} required /></label>
                </>
              )}
            </>
          )}
          {phoneChallengeSent && (
            <>
              <div className="auth-policy">Your email is verified. Enter your WhatsApp number verification code to finish creating your account.</div>
              <label>WhatsApp number<input value={phoneNumber} type="tel" autoComplete="tel" readOnly required /></label>
              <label>Verification code<input value={phoneCode} onChange={(event) => setPhoneCode(event.target.value)} inputMode="numeric" autoComplete="one-time-code" required /></label>
              {import.meta.env.DEV && <div className="demo-code-note">Local demo code: <strong>{phoneDemoCode}</strong></div>}
            </>
          )}
          {error && <div className="error-banner" role="alert">{error}</div>}
          <button className="auth-submit" type="submit" disabled={busy}>
            {busy ? 'Please wait...' : phoneChallengeSent ? 'Verify number and submit for review' : loginMode ? recoveryMode ? recoveryResult ? 'Back to sign in' : recoveryCodeSent ? 'Verify code and recover ID' : 'Send recovery code' : 'Sign in' : signupStep === 1 ? emailVerificationToken ? 'Continue to what you are here for' : emailCodeSent ? 'Resend email code' : 'Send email verification code' : signupStep === 2 ? 'Continue and create my Kahani ID' : signupStep < 6 ? 'Continue' : 'Submit for review'}
          </button>
          {loginMode && !recoveryMode && <button className="auth-back" type="button" onClick={() => { setRecoveryMode(true); setRecoveryResult(null); setError('') }}>Forgot Kahani ID?</button>}
          {loginMode && recoveryMode && !recoveryResult && recoveryCodeSent && (
            <button className="auth-back" type="button" onClick={() => void handleSendAccountIdRecoveryCode()} disabled={busy}>Resend recovery code</button>
          )}
          {loginMode && recoveryMode && !recoveryResult && <button className="auth-back" type="button" onClick={() => { setRecoveryMode(false); setRecoveryResult(null); setRecoveryCodeSent(false); setRecoveryCode(''); setError('') }}>Back to sign in</button>}
          {!loginMode && signupStep > 2 && !phoneChallengeSent && <button className="auth-back" type="button" onClick={() => { setSignupStep(signupStep - 1); setError('') }}>Back</button>}
          {phoneChallengeSent && <button className="auth-back" type="button" onClick={() => { setPhoneChallengeSent(false); setError('') }}>Back to sign-up details</button>}
        </form>
        {import.meta.env.DEV && (
          <div className="auth-demo">
            Demo sign-in: phone <strong>+15550000001</strong> or email <strong>alexmorgandemo@kahani.demo</strong>; password <strong>KahaniDemo123!</strong>. Admin email <strong>kahaniadmindemo@kahani.demo</strong>, same password.
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
          <div>
            <h2>{account.gender === 'WOMAN' ? 'Required voice recording' : 'Optional voice recording'}</h2>
            <p>{account.gender === 'WOMAN'
              ? 'A voice recording is required before the team can approve your account. Live face verification is planned for a later update.'
              : 'Your recording is private, reviewed only by admins, and deleted when they make a decision.'}</p>
          </div>
        </div>
        <VoiceUpload
          submitted={account.voiceRecordingSubmitted}
          required={account.gender === 'WOMAN'}
          onUploaded={onVoiceUploaded}
        />
      </section>
    </div>
  )
}

function VoiceUpload({ submitted, required = false, onUploaded }: {
  submitted: boolean
  required?: boolean
  onUploaded: () => void
}) {
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
      <span>{submitted
        ? 'A voice recording is ready for admin review.'
        : required
          ? 'Upload a voice recording to complete the required review materials.'
          : 'You can optionally add a short voice recording to help the admin team review your account.'}</span>
      <button className="share-button" onClick={() => inputRef.current?.click()} disabled={busy}>
        {busy ? 'Uploading...' : submitted ? 'Replace recording' : required ? 'Add required voice recording' : 'Add optional voice recording'}
      </button>
      {error && <div className="error-banner" role="alert">{error}</div>}
    </div>
  )
}

function CreditPurchasePanel({ onCreditsChanged }: { onCreditsChanged: () => void }) {
  const [packageCode, setPackageCode] = useState('FIVE_CREDITS')
  const [customCredits, setCustomCredits] = useState(1)
  const [paymentReference, setPaymentReference] = useState('')
  const [purchases, setPurchases] = useState<CreditPurchase[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const amount = packageCode === 'FIVE_CREDITS'
    ? 249
    : packageCode === 'TWENTY_CREDITS'
      ? 899
      : customCredits * 80
  const credits = packageCode === 'FIVE_CREDITS' ? 5 : packageCode === 'TWENTY_CREDITS' ? 20 : customCredits

  const refresh = () => {
    setLoading(true)
    getMyCreditPurchases()
      .then(setPurchases)
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : 'Could not load payment requests.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    refresh()
  }, [])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setMessage('')
    try {
      await requestCreditPurchase(
        packageCode,
        packageCode === 'CUSTOM' ? customCredits : null,
        paymentReference,
      )
      setPaymentReference('')
      setMessage('Payment reference submitted. Credits will be added after an admin confirms the payment.')
      refresh()
      onCreditsChanged()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not submit payment reference.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="credit-purchase-panel">
      <div className="section-heading">
        <div><h2>Get connection credits</h2><p>Men use 1 credit to request a connection with a woman. Access activates when an admin confirms payment.</p></div>
      </div>
      <div className="credit-package-grid">
        <button type="button" className={packageCode === 'FIVE_CREDITS' ? 'credit-package selected' : 'credit-package'} onClick={() => setPackageCode('FIVE_CREDITS')}>
          <strong>₹249</strong><span>5 credits</span><small>₹49.80 / credit</small>
        </button>
        <button type="button" className={packageCode === 'TWENTY_CREDITS' ? 'credit-package selected' : 'credit-package'} onClick={() => setPackageCode('TWENTY_CREDITS')}>
          <strong>₹899</strong><span>20 credits</span><small>₹44.95 / credit</small>
        </button>
        <button type="button" className={packageCode === 'CUSTOM' ? 'credit-package selected' : 'credit-package'} onClick={() => setPackageCode('CUSTOM')}>
          <strong>Custom</strong><span>₹80 / credit</span><small>Payments in whole-credit multiples</small>
        </button>
      </div>
      <form className="credit-payment-form" onSubmit={(event) => void submit(event)}>
        {packageCode === 'CUSTOM' && (
          <label>Number of credits
            <input type="number" min={1} max={10000} step={1} value={customCredits} onChange={(event) => setCustomCredits(Number(event.target.value))} required />
          </label>
        )}
        {packageCode === 'CUSTOM' && <p className="payment-total">Amount to pay: <strong>₹{amount.toLocaleString('en-IN')}</strong> for {credits} credit{credits === 1 ? '' : 's'}</p>}
        <label>UPI/payment reference<input value={paymentReference} onChange={(event) => setPaymentReference(event.target.value)} minLength={6} maxLength={120} required placeholder="Enter the transaction reference after paying" /></label>
        <p className="payment-instructions">Complete payment using your chosen payment app, then submit its reference. This app does not collect card/UPI credentials or verify payment automatically.</p>
        {error && <div className="error-banner" role="alert">{error}</div>}
        {message && <div className="notice-banner" role="status">{message}</div>}
        <button className="auth-submit" type="submit" disabled={busy || (packageCode === 'CUSTOM' && (!Number.isInteger(customCredits) || customCredits < 1 || customCredits > 10000))}>
          {busy ? 'Submitting...' : `Submit ₹${amount.toLocaleString('en-IN')} payment reference`}
        </button>
      </form>
      <div className="purchase-history">
        <h3>Your payment requests</h3>
        {loading ? <span>Loading payment history…</span> : purchases.length === 0 ? <span>No payment requests yet.</span> : purchases.slice(0, 5).map((purchase) => (
          <div className="purchase-history-row" key={purchase.id}>
            <span>₹{purchase.amountRupees.toLocaleString('en-IN')} · {purchase.credits} credits</span>
            <strong className={`purchase-status ${purchase.status.toLowerCase()}`}>{purchase.status.toLowerCase()}</strong>
          </div>
        ))}
      </div>
    </section>
  )
}

function AdminPanel() {
  const [tab, setTab] = useState<'stories' | 'payments'>('stories')
  const [queue, setQueue] = useState<VerificationCase[]>([])
  const [payments, setPayments] = useState<CreditPurchase[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)

  const refresh = () => {
    setLoading(true)
    setError('')
    const loadQueue = tab === 'stories' ? getVerificationQueue() : getCreditPurchaseQueue()
    loadQueue
      .then((items) => tab === 'stories'
        ? setQueue(items as VerificationCase[])
        : setPayments(items as CreditPurchase[]))
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : 'Could not load verification queue.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    refresh()
  }, [tab])

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

  async function decidePayment(purchaseId: number, approved: boolean) {
    setBusyId(purchaseId)
    setError('')
    try {
      await reviewCreditPurchase(purchaseId, approved)
      setPayments((current) => current.filter((entry) => entry.id !== purchaseId))
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not review this payment.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <>
      <div className="connections-intro">
        <div className="eyebrow"><span className="eyebrow-line" /> KAHANI COMMUNITY CARE</div>
        <h1>Admin dashboard<span className="title-dot">.</span></h1>
        <p className="page-description">Review member stories and manually match payment references against your payment provider before granting credits.</p>
      </div>
      <div className="admin-tabs" role="tablist" aria-label="Admin queues">
        <button type="button" className={tab === 'stories' ? 'active' : ''} onClick={() => setTab('stories')}>Story reviews <span>{queue.length}</span></button>
        <button type="button" className={tab === 'payments' ? 'active' : ''} onClick={() => setTab('payments')}>Payment reviews <span>{payments.length}</span></button>
      </div>
      {error && <div className="error-banner" role="alert">{error}</div>}
      {loading ? <div className="empty-state"><div className="loader" /> Loading {tab === 'stories' ? 'story' : 'payment'} reviews...</div> : tab === 'stories' ? queue.length === 0 ? (
        <div className="empty-state"><div className="empty-icon"><Check size={20} /></div><strong>All caught up</strong><span>There are no accounts waiting for story review.</span></div>
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
      ) : payments.length === 0 ? (
        <div className="empty-state"><div className="empty-icon"><Check size={20} /></div><strong>No payment requests</strong><span>New UPI references will appear here for manual verification.</span></div>
      ) : (
        <div className="review-queue">
          {payments.map((payment) => (
            <article className="verification-card payment-review-card" key={payment.id}>
              <div className="verification-card-top">
                <div><strong>{payment.name}</strong><span>{payment.accountId}</span></div>
                <span className="review-status-dot" /> Pending
              </div>
              <div className="payment-review-summary">
                <strong>₹{payment.amountRupees.toLocaleString('en-IN')}</strong>
                <span>{payment.credits} credits · {payment.packageCode.replaceAll('_', ' ').toLowerCase()}</span>
              </div>
              <div className="payment-reference"><span>UPI/payment reference</span><strong>{payment.paymentReference}</strong></div>
              <p className="admin-payment-warning">Check this reference in the payment provider before approving. Credits and one-time access activate only after approval.</p>
              <div className="verification-actions">
                <button className="reject-button" onClick={() => void decidePayment(payment.id, false)} disabled={busyId === payment.id}>Reject</button>
                <button className="connect-button" onClick={() => void decidePayment(payment.id, true)} disabled={busyId === payment.id}>
                  {busyId === payment.id ? 'Saving...' : 'Confirm payment & grant credits'}
                </button>
              </div>
            </article>
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
      <div className="review-member-meta">
        <span>{entry.accountId}</span>
        {entry.dateOfBirth && <span>Born {entry.dateOfBirth}</span>}
        {entry.relationshipStatus && <span>{entry.relationshipStatus.replaceAll('_', ' ').toLowerCase()}</span>}
        {entry.whatHereFor && <span>Looking for: {entry.whatHereFor.replaceAll('_', ' ').toLowerCase()}</span>}
      </div>
      <div className="review-member-meta">
        {entry.occupation && <span>{entry.occupation}</span>}
        {entry.nativeCity && <span>Native city: {entry.nativeCity}</span>}
        {entry.languages && <span>Languages: {entry.languages.replaceAll(',', ', ')}</span>}
      </div>
      <h3>{entry.headline || 'A story from me'}</h3>
      <p className="verification-story">{entry.story}</p>
      {entry.interests && <div className="review-detail"><strong>Interests</strong><span>{entry.interests.replaceAll(',', ', ')}</span></div>}
      {entry.interestNotes && <div className="review-detail"><strong>Interest notes</strong><p>{entry.interestNotes}</p></div>}
      {entry.valuesNote && <div className="review-detail"><strong>What they value</strong><p>{entry.valuesNote}</p></div>}
      <a className="verification-instagram" href={`https://www.instagram.com/${encodeURIComponent(entry.instagramHandle)}/`} target="_blank" rel="noreferrer">
        <Instagram size={15} /> @{entry.instagramHandle} <span>Review Instagram</span>
      </a>
      <div className="verification-voice">
        {entry.voiceRecordingSubmitted ? voiceUrl ? <audio controls src={voiceUrl}>Audio playback is not supported.</audio> : <span>Loading voice recording…</span> : <span>{entry.gender === 'WOMAN' ? 'Voice recording required before approval.' : 'No voice recording submitted. It is optional.'}</span>}
      </div>
      {entry.gender === 'WOMAN' && <p className="admin-payment-warning">A voice recording is required before approval. Live face verification is planned and is not part of this review yet. Do not infer identity or gender from voice.</p>}
      {error && <div className="error-banner" role="alert">{error}</div>}
      <div className="verification-actions">
        <button className="reject-button" onClick={() => onDecision(false)} disabled={busy}>Reject</button>
        <button className="connect-button" onClick={() => onDecision(true)} disabled={busy || (entry.gender === 'WOMAN' && !entry.voiceRecordingSubmitted)}>{busy ? 'Saving...' : 'Approve account'}</button>
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
    const refreshConversation = async () => {
      try {
        const [conversation, instagramShares] = await Promise.all([
          getMessages(connection.requestId),
          getInstagramShares(connection.requestId),
        ])
        if (!active) return
        setMessages(conversation)
        setShares(instagramShares)
        setError('')
      } catch (cause) {
        if (active) setError(cause instanceof Error ? cause.message : 'Could not load this conversation.')
      } finally {
        if (active) setLoading(false)
      }
    }
    void refreshConversation()
    const intervalId = window.setInterval(() => void refreshConversation(), 5000)
    return () => {
      active = false
      window.clearInterval(intervalId)
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
  const [skippedProfileIds, setSkippedProfileIds] = useState<number[]>([])
  const [favouriteProfileIds, setFavouriteProfileIds] = useState<number[]>([])
  const [currentStoryIndex, setCurrentStoryIndex] = useState(0)
  const [moreSection, setMoreSection] = useState<MoreSection>('menu')
  const [helloNotes, setHelloNotes] = useState<Record<number, string>>({})
  const [composingHelloFor, setComposingHelloFor] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [authChecked, setAuthChecked] = useState(false)
  const [busyIds, setBusyIds] = useState<number[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    getCurrentProfile()
      .then((currentAccount) => {
        if (!active) return
        setAccount(currentAccount)
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
    const refreshAccountAndConnections = async () => {
      try {
        const currentAccount = await getCurrentProfile()
        if (!active) return
        setAccount(currentAccount)
        if (currentAccount.verificationStatus !== 'APPROVED') {
          setConnections([])
          return
        }
        const currentConnections = await getConnections()
        if (active) setConnections(currentConnections)
      } catch (cause) {
        if (active) setError(cause instanceof Error ? cause.message : 'Could not refresh your account.')
      }
    }
    const intervalId = window.setInterval(() => void refreshAccountAndConnections(), 15000)
    window.addEventListener('focus', refreshAccountAndConnections)
    return () => {
      active = false
      window.clearInterval(intervalId)
      window.removeEventListener('focus', refreshAccountAndConnections)
    }
  }, [account?.profile.id])

  useEffect(() => {
    if (!account || account.accountStatus === 'PENDING_VERIFICATION') return
    let active = true
    getDiscoverProfiles('')
      .then((results) => {
        if (active) setProfiles(results)
      })
      .catch((cause: unknown) => {
        if (active) setError(cause instanceof Error ? cause.message : 'Could not load people.')
      })
    return () => {
      active = false
    }
  }, [account?.profile.id, account?.verificationStatus, account?.accountStatus])

  useEffect(() => {
    if (!account) return
    try {
      const saved = sessionStorage.getItem(`kahani-skipped-profiles-${account.profile.id}`)
      const parsed: unknown = saved ? JSON.parse(saved) : []
      setSkippedProfileIds(Array.isArray(parsed) ? parsed.filter((id): id is number => Number.isInteger(id)) : [])
    } catch (cause) {
      setError(cause instanceof Error ? 'Could not restore skipped profiles for this session.' : 'Could not restore skipped profiles.')
    }
  }, [account?.profile.id])

  useEffect(() => {
    if (!account) return
    try {
      const saved = localStorage.getItem(`kahani-favourites-${account.profile.id}`)
      const parsed: unknown = saved ? JSON.parse(saved) : []
      setFavouriteProfileIds(Array.isArray(parsed) ? parsed.filter((id): id is number => Number.isInteger(id)) : [])
    } catch (cause) {
      setError(cause instanceof Error ? 'Could not restore your favourites.' : 'Could not restore favourites.')
    }
  }, [account?.profile.id])

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
  const visibleProfiles = useMemo(
    () => profiles.filter((profile) => !skippedProfileIds.includes(profile.id)),
    [profiles, skippedProfileIds],
  )

  useEffect(() => {
    setCurrentStoryIndex((index) => Math.min(index, Math.max(0, visibleProfiles.length - 1)))
  }, [visibleProfiles.length])

  function handleSkip(profileId: number) {
    const updated = [...new Set([...skippedProfileIds, profileId])]
    setSkippedProfileIds(updated)
    try {
      sessionStorage.setItem(`kahani-skipped-profiles-${account?.profile.id}`, JSON.stringify(updated))
    } catch (cause) {
      setError(cause instanceof Error ? 'Could not save your skipped profile in this browser session.' : 'Could not save your skipped profile.')
    }
    setComposingHelloFor((current) => current === profileId ? null : current)
  }

  function handleUnskip(profileId: number) {
    if (!account) return
    const updated = skippedProfileIds.filter((id) => id !== profileId)
    setSkippedProfileIds(updated)
    try {
      sessionStorage.setItem(`kahani-skipped-profiles-${account.profile.id}`, JSON.stringify(updated))
    } catch (cause) {
      setError(cause instanceof Error ? 'Could not update skipped profiles in this browser session.' : 'Could not update skipped profiles.')
    }
  }

  function handleFavourite(profileId: number) {
    if (!account) return
    const updated = favouriteProfileIds.includes(profileId)
      ? favouriteProfileIds.filter((id) => id !== profileId)
      : [...favouriteProfileIds, profileId]
    setFavouriteProfileIds(updated)
    try {
      localStorage.setItem(`kahani-favourites-${account.profile.id}`, JSON.stringify(updated))
    } catch (cause) {
      setError(cause instanceof Error ? 'Could not save your favourites.' : 'Could not save favourites.')
    }
  }

  async function handleConnect(profileId: number) {
    setBusyIds((ids) => [...ids, profileId])
    setError('')
    try {
      const note = (helloNotes[profileId] ?? '').trim()
      const existing = connectionByProfile.get(profileId)
      let updatedConnection: Connection
      if (existing?.direction === 'CONNECTED') {
        await sendMessage(existing.requestId, note)
        updatedConnection = existing
        setActiveChat(existing)
        setView('chat')
      } else if (existing?.direction === 'INCOMING') {
        updatedConnection = await acceptConnection(existing.requestId)
        setConnections((current) => current.map((item) =>
          item.requestId === updatedConnection.requestId ? updatedConnection : item))
        await sendMessage(updatedConnection.requestId, note)
        setActiveChat(updatedConnection)
        setView('chat')
      } else {
        updatedConnection = await requestConnection(profileId, note)
      }
      if (existing?.direction !== 'CONNECTED') {
        const currentAccount = await getCurrentProfile()
        setAccount(currentAccount)
      }
      setComposingHelloFor(null)
      setHelloNotes((current) => ({ ...current, [profileId]: '' }))
      setConnections((current) => [
        updatedConnection,
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
          <button className={view === 'messages' || view === 'chat' ? 'nav-item active' : 'nav-item'} onClick={() => setView('messages')}>
            <MessageCircle size={18} />
            <span>Messages</span>
            <span className="nav-count">{connectedCount}</span>
          </button>
          <button className={view === 'more' ? 'nav-item active' : 'nav-item'} onClick={() => { setView('more'); setMoreSection('menu') }}>
            <MoreHorizontal size={18} />
            <span>More</span>
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
          <div className="breadcrumb"><span>KAHANI</span><span className="breadcrumb-separator">/</span><strong>{view === 'admin' ? 'ADMIN REVIEW' : view === 'discover' ? 'STORIES' : view === 'chat' || view === 'messages' ? 'MESSAGES' : view === 'more' ? 'MORE' : 'CONNECTIONS'}</strong></div>
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
          {view === 'more' ? (
            <MoreView
              section={moreSection}
              onSectionChange={setMoreSection}
              isMan={account.gender === 'MAN'}
              favourites={profiles.filter((profile) => favouriteProfileIds.includes(profile.id))}
              skipped={profiles.filter((profile) => skippedProfileIds.includes(profile.id))}
              matches={connections.filter((connection) => connection.direction === 'CONNECTED')}
              onRemoveFavourite={handleFavourite}
              onRestoreSkipped={handleUnskip}
              onMessage={(connection) => { setActiveChat(connection); setView('chat') }}
              onDiscover={() => setView('discover')}
              onLogout={() => void handleLogout()}
              onCreditsChanged={() => {
                getCurrentProfile().then(setAccount).catch((cause: unknown) =>
                  setError(cause instanceof Error ? cause.message : 'Could not refresh account credits.'))
              }}
            />
          ) : view === 'admin' && account.admin ? (
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

              {error && <div className="error-banner" role="alert">{error}</div>}
              <div className="section-heading">
                <div><h2>Stories to discover</h2><p>{visibleProfiles.length > 0 ? `Story ${currentStoryIndex + 1} of ${visibleProfiles.length}` : 'New stories will appear here'}</p></div>
              </div>

              {loading ? (
                <div className="empty-state"><div className="loader" /><span>Finding your people...</span></div>
              ) : visibleProfiles.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon"><Sparkles size={21} /></div>
                  <strong>You’ve reached the end of the stories</strong>
                  <span>Check back later for more people to meet.</span>
                </div>
              ) : (
                <div className="people-grid">
                  {visibleProfiles.slice(currentStoryIndex, currentStoryIndex + 1).map((person) => {
                    const connection = connectionByProfile.get(person.id)
                    const isBusy = busyIds.includes(person.id)
                    return (
                      <article className="person-card" key={person.id}>
                        <div className="card-top">
                          <div className="match-pill"><Sparkles size={12} /> {person.matchPercent}% match</div>
                          <button
                            type="button"
                            className={favouriteProfileIds.includes(person.id) ? 'favourite-toggle active' : 'favourite-toggle'}
                            onClick={() => handleFavourite(person.id)}
                            aria-label={favouriteProfileIds.includes(person.id) ? `Remove ${person.name} from favourites` : `Add ${person.name} to favourites`}
                            aria-pressed={favouriteProfileIds.includes(person.id)}
                          >
                            <Heart size={16} fill={favouriteProfileIds.includes(person.id) ? 'currentColor' : 'none'} />
                          </button>
                        </div>
                        <div className="person-identity">
                          <Avatar profile={person} size="large" />
                          <div className="person-heading">
                            <h3>{person.name}</h3>
                            <p>{person.occupation || person.headline}</p>
                            <span className="person-location"><MapPin size={12} />{person.city}</span>
                            <div className="public-profile-facts">
                              {[person.age !== null ? `${person.age}` : null, person.gender, person.relationshipStatus]
                                .filter((fact): fact is string => Boolean(fact))
                                .map((fact) => <span key={fact}>{fact.replaceAll('_', ' ').toLowerCase()}</span>)}
                            </div>
                          </div>
                        </div>
                        {(person.whatHereFor.length > 0 || person.nativeCity || person.languages.length > 0) && (
                          <div className="public-profile-details">
                            {person.whatHereFor.length > 0 && <div><span className="field-label">HERE FOR</span><p>{person.whatHereFor.map(formatProfileChoice).join(' · ')}</p></div>}
                            {person.nativeCity && <div><span className="field-label">NATIVE CITY</span><p>{person.nativeCity}</p></div>}
                            {person.languages.length > 0 && <div><span className="field-label">LANGUAGES</span><p>{person.languages.join(' · ')}</p></div>}
                          </div>
                        )}
                        <div className="story-excerpt"><span className="field-label">A STORY FROM {person.name.toUpperCase()}</span><p>{person.story}</p></div>
                        {connection?.direction === 'INCOMING' && connection.introNote && (
                          <div className="incoming-hello-note"><span className="field-label">THEIR HELLO</span><p>{connection.introNote}</p></div>
                        )}
                        <div className="interests-block">
                          <span className="field-label">INTERESTS</span>
                          <div className="interest-list">
                            {person.interests.map((interest) => (
                              <span className={person.sharedInterests.includes(interest.toLowerCase()) || person.sharedInterests.includes(interest) ? 'interest-tag shared' : 'interest-tag'} key={interest}>
                                {interest}
                              </span>
                            ))}
                          </div>
                          {person.interestNotes.length > 0 && <div className="public-interest-notes">
                            {person.interestNotes.map((note) => <p key={note}>{note}</p>)}
                          </div>}
                        </div>
                        {person.valuesNote && <div className="public-values-note"><span className="field-label">WHAT MATTERS MOST</span><p>{person.valuesNote}</p></div>}
                        <div className="card-footer">
                          <div className="shared-caption">
                            {person.sharedInterests.length > 0
                              ? <><span className="shared-avatar"><Heart size={11} fill="currentColor" /></span>{person.sharedInterests.slice(0, 2).join(' · ')}</>
                              : 'A new perspective awaits'}
                          </div>
                          <div className="discover-actions">
                            <button type="button" className="skip-button" onClick={() => handleSkip(person.id)}>Skip</button>
                            <button
                              type="button"
                              className="connect-button"
                              onClick={() => setComposingHelloFor((current) => current === person.id ? null : person.id)}
                              disabled={isBusy}
                            >
                              Say hello <ArrowRight size={14} />
                            </button>
                          </div>
                        </div>
                        {composingHelloFor === person.id && (
                          <form className="hello-note-form" onSubmit={(event) => { event.preventDefault(); void handleConnect(person.id) }}>
                            <label htmlFor={`hello-note-${person.id}`}>Write a short note before saying hello</label>
                            <textarea
                              id={`hello-note-${person.id}`}
                              value={helloNotes[person.id] ?? ''}
                              onChange={(event) => setHelloNotes((current) => ({ ...current, [person.id]: event.target.value }))}
                              minLength={10}
                              maxLength={500}
                              required
                              placeholder="What stood out to you in their story?"
                              rows={3}
                            />
                            <div className="hello-note-footer">
                              <span>{(helloNotes[person.id] ?? '').length}/500 · at least 10 characters{connection?.direction === 'OUTGOING' ? ' · updates your pending hello' : connection?.direction === 'INCOMING' ? ' · accepts their request and starts a chat' : connection?.direction === 'CONNECTED' ? ' · sends this as a message' : ''}</span>
                              <button className="connect-button" type="submit" disabled={isBusy || (helloNotes[person.id] ?? '').trim().length < 10}>
                                {isBusy ? 'Sending...' : connection?.direction === 'CONNECTED' ? 'Send message' : connection?.direction === 'INCOMING' ? 'Accept & say hello' : connection?.direction === 'OUTGOING' ? 'Update hello note' : 'Send hello'}
                              </button>
                            </div>
                          </form>
                        )}
                      </article>
                    )
                  })}
                </div>
              )}
              {!loading && visibleProfiles.length > 0 && (
                <div className="story-navigation" aria-label="Story navigation">
                  <button
                    type="button"
                    className="skip-button"
                    onClick={() => setCurrentStoryIndex((index) => Math.max(0, index - 1))}
                    disabled={currentStoryIndex === 0}
                  >
                    <ArrowLeft size={14} /> Previous
                  </button>
                  <button
                    type="button"
                    className="connect-button"
                    onClick={() => setCurrentStoryIndex((index) => Math.min(visibleProfiles.length - 1, index + 1))}
                    disabled={currentStoryIndex >= visibleProfiles.length - 1}
                  >
                    Next story <ArrowRight size={14} />
                  </button>
                </div>
              )}
              <div className="bottom-note"><Sparkles size={14} /> A good connection can start with one small hello.</div>
            </>
          ) : view === 'connections' || view === 'messages' ? (
            <ConnectionsView
              key={view}
              connections={connections}
              busyIds={busyIds}
              error={error}
              incomingCount={incomingCount}
              connectedCount={connectedCount}
              initialFilter={view === 'messages' ? 'connected' : 'all'}
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
  initialFilter = 'all',
  onAccept,
  onMessage,
}: {
  connections: Connection[]
  busyIds: number[]
  error: string
  incomingCount: number
  connectedCount: number
  initialFilter?: 'all' | 'connected'
  onAccept: (connection: Connection) => void
  onMessage: (connection: Connection) => void
}) {
  const [filter, setFilter] = useState<'all' | 'requests' | 'connected'>(initialFilter)
  const visible = connections.filter((connection) => {
    if (filter === 'requests') return connection.direction === 'INCOMING'
    if (filter === 'connected') return connection.direction === 'CONNECTED'
    return true
  })

  return (
    <>
      <div className="connections-intro">
        <div className="eyebrow"><span className="eyebrow-line" /> {initialFilter === 'connected' ? 'YOUR MATCHES, READY TO CHAT' : 'YOUR PEOPLE, YOUR PACE'}</div>
        <h1>{initialFilter === 'connected' ? 'Messages' : 'Connections'}<span className="title-dot">.</span></h1>
        <p className="page-description">{initialFilter === 'connected' ? 'After a match, you can start a private conversation here.' : 'Every good friendship starts somewhere. See who’s reaching out.'}</p>
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
          <strong>{filter === 'requests' ? 'No new requests' : filter === 'connected' ? 'No matches to message yet' : 'Your next connection is out there'}</strong>
          <span>{filter === 'requests' ? 'When someone reaches out, you’ll find them here.' : filter === 'connected' ? 'When you and someone accept a connection, message them here.' : 'Explore people who share the things you love.'}</span>
        </div>
      ) : (
        <div className="connection-list">
          {visible.map((connection) => (
            <article className="connection-row" key={connection.requestId}>
              <Avatar profile={connection.profile} size="medium" />
              <div className="connection-copy">
                <div className="connection-name">{connection.profile.name}<span>{connection.direction === 'INCOMING' ? 'wants to connect' : connection.direction === 'CONNECTED' ? 'you’re connected' : 'request sent'}</span></div>
                <p>{connection.profile.headline}</p>
                {connection.introNote && <div className="connection-intro-note"><strong>{connection.direction === 'INCOMING' ? 'Their hello' : 'Hello note'}</strong><span>{connection.introNote}</span></div>}
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

function MoreView({
  section,
  onSectionChange,
  isMan,
  favourites,
  skipped,
  matches,
  onRemoveFavourite,
  onRestoreSkipped,
  onMessage,
  onLogout,
  onDiscover,
  onCreditsChanged,
}: {
  section: MoreSection
  onSectionChange: (section: MoreSection) => void
  isMan: boolean
  favourites: Profile[]
  skipped: Profile[]
  matches: Connection[]
  onRemoveFavourite: (profileId: number) => void
  onRestoreSkipped: (profileId: number) => void
  onMessage: (connection: Connection) => void
  onLogout: () => void
  onDiscover: () => void
  onCreditsChanged: () => void
}) {
  const menu = [
    { id: 'matches' as const, label: 'Matches', description: 'People you matched with', count: matches.length, icon: <Users size={18} /> },
    { id: 'favourites' as const, label: 'Favourites', description: 'Profiles you saved', count: favourites.length, icon: <Star size={18} /> },
    { id: 'skipped' as const, label: 'Skipped profiles', description: 'Profiles skipped this session', count: skipped.length, icon: <ArrowRight size={18} /> },
    ...(isMan ? [{ id: 'membership' as const, label: 'Membership', description: 'Credits and payment requests', icon: <Wallet size={18} /> }] : []),
  ]

  return (
    <>
      <div className="connections-intro more-intro">
        <div className="eyebrow"><span className="eyebrow-line" /> YOUR KAHANI, YOUR WAY</div>
        <h1>More<span className="title-dot">.</span></h1>
        <p className="page-description">Manage your matches, saved stories, and account.</p>
      </div>
      <div className="more-layout">
        <nav className="more-menu" aria-label="More options">
          {menu.map((item) => (
            <button
              type="button"
              key={item.id}
              className={section === item.id ? 'more-menu-item active' : 'more-menu-item'}
              onClick={() => onSectionChange(item.id)}
            >
              <span className="more-menu-icon">{item.icon}</span>
              <span className="more-menu-copy"><strong>{item.label}</strong><small>{item.description}</small></span>
              {'count' in item && <span className="more-menu-count">{item.count}</span>}
            </button>
          ))}
          <button type="button" className="more-menu-item logout-menu-item" onClick={onLogout}>
            <span className="more-menu-icon"><LogOut size={18} /></span>
            <span className="more-menu-copy"><strong>Log out</strong><small>Sign out of your account</small></span>
          </button>
        </nav>

        <section className="more-content">
          {section === 'menu' && (
            <div className="more-empty-prompt">
              <div className="empty-icon"><MoreHorizontal size={20} /></div>
              <strong>Everything in one place</strong>
              <span>Choose an option to see your matches, favourites, or skipped profiles.</span>
            </div>
          )}
          {section === 'matches' && (
            <>
              <div className="more-section-heading"><h2>Matches</h2><p>Start a conversation with someone you’ve connected with.</p></div>
              {matches.length === 0 ? <MoreEmpty title="No matches yet" text="When both people accept a connection, it will appear here." /> : (
                <div className="more-record-list">
                  {matches.map((match) => (
                    <article className="more-record" key={match.requestId}>
                      <Avatar profile={match.profile} size="medium" />
                      <div className="more-record-copy">
                        <strong>{match.profile.name}</strong>
                        <span>{match.profile.headline}</span>
                        {match.introNote && <p>{match.introNote}</p>}
                      </div>
                      <button type="button" className="connect-button" onClick={() => onMessage(match)}><MessageCircle size={14} /> Message</button>
                    </article>
                  ))}
                </div>
              )}
            </>
          )}
          {section === 'favourites' && (
            <>
              <div className="more-section-heading"><h2>Favourites</h2><p>Stories you’ve saved to revisit.</p></div>
              {favourites.length === 0 ? <MoreEmpty title="No favourites saved" text="Tap the heart on a Discover profile to save it here." /> : (
                <div className="more-record-list">
                  {favourites.map((profile) => (
                    <article className="more-record saved-profile" key={profile.id}>
                      <Avatar profile={profile} size="medium" />
                      <div className="more-record-copy">
                        <strong>{profile.name}</strong>
                        <span>{profile.occupation || profile.headline} · {profile.city}</span>
                        <p>{profile.story}</p>
                      </div>
                      <div className="more-record-actions">
                        <button type="button" className="share-button" onClick={onDiscover}>Discover</button>
                        <button type="button" className="skip-button" onClick={() => onRemoveFavourite(profile.id)}>Remove</button>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </>
          )}
          {section === 'skipped' && (
            <>
              <div className="more-section-heading"><h2>Skipped profiles</h2><p>Bring a skipped profile back to Discover whenever you like.</p></div>
              {skipped.length === 0 ? <MoreEmpty title="No skipped profiles" text="Profiles you skip in Discover will show up here for this session." /> : (
                <div className="more-record-list">
                  {skipped.map((profile) => (
                    <article className="more-record saved-profile" key={profile.id}>
                      <Avatar profile={profile} size="medium" />
                      <div className="more-record-copy">
                        <strong>{profile.name}</strong>
                        <span>{profile.occupation || profile.headline} · {profile.city}</span>
                        <p>{profile.story}</p>
                      </div>
                      <button type="button" className="connect-button" onClick={() => { onRestoreSkipped(profile.id); onDiscover() }}>Restore</button>
                    </article>
                  ))}
                </div>
              )}
            </>
          )}
          {section === 'membership' && isMan && (
            <>
              <div className="more-section-heading"><h2>Membership</h2><p>Manage your credits and payment requests.</p></div>
              <CreditPurchasePanel onCreditsChanged={onCreditsChanged} />
            </>
          )}
        </section>
      </div>
    </>
  )
}

function MoreEmpty({ title, text }: { title: string; text: string }) {
  return <div className="more-empty-prompt"><div className="empty-icon"><Heart size={20} /></div><strong>{title}</strong><span>{text}</span></div>
}

function formatProfileChoice(value: string) {
  return value.split('_').map((word) => word.charAt(0) + word.slice(1).toLowerCase()).join(' ')
}
