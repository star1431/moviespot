'use client';

import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';
import { usePathname } from 'next/navigation';
import { Film, User, LogOut } from 'lucide-react';

export default function Header() {
  const { user, isAuthenticated, logout } = useAuth();
  const pathname = usePathname();

  const handleLogout = async () => {
    await logout();
    // 쿠키 삭제가 완료되도록 약간의 지연 후 페이지 이동
    setTimeout(() => {
      window.location.href = '/';
    }, 100);
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-white shadow-sm">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* 로고 */}
          <Link href="/" className="flex items-center gap-2">
            <Film className="h-6 w-6 text-blue-600" />
            <span className="text-xl font-bold text-gray-900">MovieSpot</span>
          </Link>

          {/* 메뉴 */}
          <nav className="hidden md:flex items-center gap-6">
            <Link
              href="/"
              className={`text-sm font-medium transition-colors hover:text-blue-600 ${
                pathname === '/' ? 'text-blue-600' : 'text-gray-700'
              }`}
            >
              홈
            </Link>
            <Link
              href="/movies"
              className={`text-sm font-medium transition-colors hover:text-blue-600 ${
                pathname === '/movies' ? 'text-blue-600' : 'text-gray-700'
              }`}
            >
              영화 목록
            </Link>
            <Link
              href="/reviews"
              className={`text-sm font-medium transition-colors hover:text-blue-600 ${
                pathname?.startsWith('/reviews') ? 'text-blue-600' : 'text-gray-700'
              }`}
            >
              리뷰
            </Link>
            <Link
              href="/collection"
              className={`text-sm font-medium transition-colors hover:text-blue-600 ${
                pathname === '/collection' ? 'text-blue-600' : 'text-gray-700'
              }`}
            >
              나의 컬렉션
            </Link>
          </nav>

          {/* 로그인/사용자 정보 */}
          <div className="flex items-center gap-4">
            {isAuthenticated ? (
              <>
                <div className="hidden md:flex items-center gap-2 text-sm text-gray-700">
                  <User className="h-4 w-4" />
                  <span>{user?.nickname || user?.email || '사용자'}</span>
                </div>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-100"
                >
                  <LogOut className="h-4 w-4" />
                  <span className="hidden sm:inline">로그아웃</span>
                </button>
              </>
            ) : (
              <Link
                href="/login"
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-blue-700"
              >
                로그인
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}

