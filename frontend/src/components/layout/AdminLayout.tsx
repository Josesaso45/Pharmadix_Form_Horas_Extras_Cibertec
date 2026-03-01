import { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import {
    LayoutDashboard,
    History,
    Users,
    Calculator,
    BarChart,
    Settings,
    Activity
} from 'lucide-react';
import { cn } from '@/lib/utils';

interface AdminLayoutProps {
    children: ReactNode;
}

export default function AdminLayout({ children }: AdminLayoutProps) {
    const navItems = [
        { name: 'Dashboard', icon: LayoutDashboard, path: '/admin/dashboard' },
        { name: 'Hojas de Tiempo', icon: History, path: '/admin/hojas' },
        { name: 'Operarios', icon: Users, path: '/admin/operarios' },
        { name: 'Cálculo H-H', icon: Calculator, path: '/admin/calculo-hh' },
        { name: 'Reportes', icon: BarChart, path: '/admin/reportes' },
        { name: 'Configuración', icon: Settings, path: '/admin/configuracion' },
    ];

    return (
        <div className="flex min-h-screen bg-slate-50 text-slate-900 font-sans">
            {/* Sidebar */}
            <aside className="w-64 bg-[#1a227f] text-white flex flex-col fixed h-full z-20">
                <div className="p-6 flex items-center gap-3">
                    <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center">
                        <Activity className="text-[#1a227f] w-6 h-6" />
                    </div>
                    <div>
                        <h1 className="text-lg font-bold leading-none uppercase tracking-wider">Pharmadix</h1>
                        <p className="text-[10px] text-white/70">TIMES ADMIN</p>
                    </div>
                </div>

                <nav className="flex-1 px-4 py-6 space-y-2">
                    {navItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) =>
                                cn(
                                    "flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors font-medium text-sm",
                                    isActive
                                        ? "bg-[#7B2FBE] text-white shadow-lg"
                                        : "text-white/80 hover:bg-white/10"
                                )
                            }
                        >
                            <item.icon className="w-5 h-5" />
                            <span>{item.name}</span>
                        </NavLink>
                    ))}
                </nav>

                <div className="p-6 border-t border-white/10">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-white/20 overflow-hidden flex items-center justify-center font-bold">
                            AU
                        </div>
                        <div>
                            <p className="text-sm font-bold">Admin Usuario</p>
                            <p className="text-xs text-white/60">Planta Lima</p>
                        </div>
                    </div>
                </div>
            </aside>

            {/* Main Content */}
            <main className="ml-64 flex-1 flex flex-col min-h-screen">
                {children}
            </main>
        </div>
    );
}
